package com.mkfmm.resource.application.service;

import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.shared.exception.BusinessException;
import com.mkfmm.shared.exception.ForbiddenException;
import com.mkfmm.resource.application.port.inbound.ResourceUseCase;
import com.mkfmm.resource.application.port.outbound.EventPublisherPort;
import com.mkfmm.resource.application.port.outbound.ResourceRepository;
import com.mkfmm.resource.domain.model.*;
import com.mkfmm.resource.domain.service.LevenshteinCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ResourceAppService implements ResourceUseCase {

    private final ResourceRepository resourceRepository;
    private final EventPublisherPort eventPublisher;
    private final LevenshteinCalculator levenshteinCalculator;
    private final int similarityThreshold;
    private final int similarityMaxResults;

    public ResourceAppService(ResourceRepository resourceRepository,
                              EventPublisherPort eventPublisher,
                              LevenshteinCalculator levenshteinCalculator,
                              @Value("${app.similarity.threshold}") int similarityThreshold,
                              @Value("${app.similarity.max-results}") int similarityMaxResults) {
        this.resourceRepository = resourceRepository;
        this.eventPublisher = eventPublisher;
        this.levenshteinCalculator = levenshteinCalculator;
        this.similarityThreshold = similarityThreshold;
        this.similarityMaxResults = similarityMaxResults;
    }

    @Override
    public Object createResource(String resourceKey, String resourceType, String content,
                                 String description, String requesterId, String requesterRole) {
        validateWriteAccess(requesterRole);

        if (resourceRepository.existsByResourceKeyAndNotDeleted(resourceKey)) {
            Resource existing = resourceRepository.findByResourceKeyAndNotDeleted(resourceKey).orElse(null);
            return DuplicateCheckResult.duplicate(existing, "resourceKey");
        }

        List<Resource> candidates = resourceRepository.findCandidatesForSimilarity(resourceKey);
        List<SimilarityResult> similar = levenshteinCalculator.findSimilar(
                resourceKey, content, candidates, similarityThreshold, similarityMaxResults);

        if (!similar.isEmpty()) {
            return similar;
        }

        return saveNewResource(resourceKey, resourceType, content, description, requesterId);
    }

    @Override
    public Object confirmSimilarityChoice(String resourceKey, String resourceType, String content,
                                           String description, String chosenAction,
                                           String chosenResourceId, String reason,
                                           String requesterId, String requesterRole) {
        validateWriteAccess(requesterRole);

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "SimilarityChoiceMade", Instant.now(), requesterId,
                        Map.of("inputKey", resourceKey, "chosenAction", chosenAction,
                                "chosenResourceId", chosenResourceId != null ? chosenResourceId : "",
                                "reason", reason != null ? reason : "")),
                "resource.similarity.choice");

        if ("USE_EXISTING".equals(chosenAction)) {
            return resourceRepository.findById(chosenResourceId)
                    .orElseThrow(() -> new BusinessException("Chosen resource not found"));
        }

        return saveNewResource(resourceKey, resourceType, content, description, requesterId);
    }

    @Override
    public Resource updateResource(String id, String content, String description,
                                   String requesterId, String requesterRole) {
        validateWriteAccess(requesterRole);

        Resource resource = resourceRepository.findById(id)
                .filter(Resource::isActive)
                .orElseThrow(() -> new BusinessException("Resource not found: " + id));

        String previousContent = resource.getContent();
        resource.update(content, description, requesterId);
        Resource saved = resourceRepository.save(resource);

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "ResourceUpdated", Instant.now(), requesterId,
                        Map.of("resourceId", id, "resourceKey", saved.getResourceKey(),
                                "previousContent", previousContent, "newContent", saved.getContent())),
                "resource.updated");

        return saved;
    }

    @Override
    public void deleteResource(String id, String requesterId, String requesterRole) {
        validateWriteAccess(requesterRole);

        Resource resource = resourceRepository.findById(id)
                .filter(Resource::isActive)
                .orElseThrow(() -> new BusinessException("Resource not found: " + id));

        resource.softDelete();
        resourceRepository.save(resource);

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "ResourceDeleted", Instant.now(), requesterId,
                        Map.of("resourceId", id, "resourceKey", resource.getResourceKey())),
                "resource.deleted");
    }

    @Override
    public Resource getResourceById(String id) {
        return resourceRepository.findById(id)
                .filter(Resource::isActive)
                .orElseThrow(() -> new BusinessException("Resource not found: " + id));
    }

    private Resource saveNewResource(String resourceKey, String resourceType, String content,
                                     String description, String requesterId) {
        Resource resource = new Resource(resourceKey, ResourceType.valueOf(resourceType),
                content, description, requesterId);
        Resource saved = resourceRepository.save(resource);

        eventPublisher.publish(
                new BaseEvent(UUID.randomUUID().toString(), "ResourceCreated", Instant.now(), requesterId,
                        Map.of("resourceId", saved.getId(), "resourceKey", resourceKey,
                                "resourceType", resourceType, "content", content)),
                "resource.created");

        return saved;
    }

    private void validateWriteAccess(String requesterRole) {
        if ("READ".equals(requesterRole)) {
            throw new ForbiddenException("Write access required");
        }
    }
}
