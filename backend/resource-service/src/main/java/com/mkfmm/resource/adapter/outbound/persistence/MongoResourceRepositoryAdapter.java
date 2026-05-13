package com.mkfmm.resource.adapter.outbound.persistence;

import com.mkfmm.resource.adapter.outbound.persistence.document.ResourceDocument;
import com.mkfmm.resource.adapter.outbound.persistence.repository.SpringDataResourceRepository;
import com.mkfmm.resource.application.port.outbound.ResourceRepository;
import com.mkfmm.resource.domain.model.Resource;
import com.mkfmm.resource.domain.model.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MongoResourceRepositoryAdapter implements ResourceRepository {

    private final SpringDataResourceRepository springDataRepo;
    private final MongoTemplate mongoTemplate;

    public MongoResourceRepositoryAdapter(SpringDataResourceRepository springDataRepo, MongoTemplate mongoTemplate) {
        this.springDataRepo = springDataRepo;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Resource save(Resource resource) {
        ResourceDocument doc = toDocument(resource);
        ResourceDocument saved = springDataRepo.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<Resource> findById(String id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Resource> findByResourceKeyAndNotDeleted(String resourceKey) {
        return springDataRepo.findByResourceKeyAndDeletedFalse(resourceKey).map(this::toDomain);
    }

    @Override
    public boolean existsByResourceKeyAndNotDeleted(String resourceKey) {
        return springDataRepo.existsByResourceKeyAndDeletedFalse(resourceKey);
    }

    @Override
    public Page<Resource> search(String keyword, String resourceType, String createdBy,
                                 Instant startDate, Instant endDate, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("deleted").is(false));

        if (keyword != null && !keyword.isBlank()) {
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("resourceKey").regex(keyword, "i"),
                    Criteria.where("content").regex(keyword, "i")));
        }
        if (resourceType != null) criteriaList.add(Criteria.where("resourceType").is(resourceType));
        if (createdBy != null) criteriaList.add(Criteria.where("createdBy").is(createdBy));
        if (startDate != null) criteriaList.add(Criteria.where("createdAt").gte(startDate));
        if (endDate != null) criteriaList.add(Criteria.where("createdAt").lte(endDate));

        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        query.with(pageable);

        List<ResourceDocument> docs = mongoTemplate.find(query, ResourceDocument.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ResourceDocument.class);

        return PageableExecutionUtils.getPage(
                docs.stream().map(this::toDomain).toList(), pageable, () -> count);
    }

    @Override
    public List<Resource> findCandidatesForSimilarity(String keyword) {
        return springDataRepo.findByDeletedFalse().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long hardDeleteByDeletedAtBefore(Instant cutoff) {
        return springDataRepo.deleteByDeletedTrueAndDeletedAtBefore(cutoff);
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }

    private ResourceDocument toDocument(Resource r) {
        ResourceDocument doc = new ResourceDocument();
        doc.setId(r.getId());
        doc.setResourceKey(r.getResourceKey());
        doc.setResourceType(r.getResourceType().name());
        doc.setContent(r.getContent());
        doc.setDescription(r.getDescription());
        doc.setDeleted(r.isDeleted());
        doc.setDeletedAt(r.getDeletedAt());
        doc.setCreatedBy(r.getCreatedBy());
        doc.setCreatedAt(r.getCreatedAt());
        doc.setUpdatedBy(r.getUpdatedBy());
        doc.setUpdatedAt(r.getUpdatedAt());
        return doc;
    }

    private Resource toDomain(ResourceDocument doc) {
        Resource r = new Resource(doc.getResourceKey(), ResourceType.valueOf(doc.getResourceType()),
                doc.getContent(), doc.getDescription(), doc.getCreatedBy());
        r.setId(doc.getId());
        return r;
    }
}
