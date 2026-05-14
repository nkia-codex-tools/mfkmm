package com.resourcemanager.version.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resourcemanager.common.exception.BadRequestException;
import com.resourcemanager.common.exception.NotFoundException;
import com.resourcemanager.history.service.HistoryService;
import com.resourcemanager.resource.entity.FunctionResource;
import com.resourcemanager.resource.entity.MenuResource;
import com.resourcemanager.resource.entity.MessageResource;
import com.resourcemanager.resource.repository.FunctionRepository;
import com.resourcemanager.resource.repository.MenuRepository;
import com.resourcemanager.resource.repository.MessageResourceRepository;
import com.resourcemanager.version.dto.*;
import com.resourcemanager.version.entity.VersionTag;
import com.resourcemanager.version.repository.VersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VersionService {

    private static final Logger log = LoggerFactory.getLogger(VersionService.class);

    private final VersionRepository versionRepository;
    private final FunctionRepository functionRepository;
    private final MenuRepository menuRepository;
    private final MessageResourceRepository messageResourceRepository;
    private final HistoryService historyService;
    private final ObjectMapper objectMapper;

    public VersionService(VersionRepository versionRepository,
                          FunctionRepository functionRepository,
                          MenuRepository menuRepository,
                          MessageResourceRepository messageResourceRepository,
                          HistoryService historyService,
                          ObjectMapper objectMapper) {
        this.versionRepository = versionRepository;
        this.functionRepository = functionRepository;
        this.menuRepository = menuRepository;
        this.messageResourceRepository = messageResourceRepository;
        this.historyService = historyService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<VersionResponse> getVersions(String resourceType) {
        if (resourceType != null && !resourceType.isBlank()) {
            return versionRepository.findByResourceTypeOrderByCreatedAtDesc(resourceType).stream()
                    .map(VersionResponse::from).toList();
        }
        return versionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(VersionResponse::from).toList();
    }

    @Transactional
    public VersionResponse createTag(VersionTagRequest request, Long userId) {
        String snapshot = createSnapshot(request.resourceType());

        VersionTag tag = new VersionTag();
        tag.setTagName(request.tagName());
        tag.setDescription(request.description());
        tag.setResourceType(request.resourceType());
        tag.setSnapshotData(snapshot);
        tag.setCreatedBy(userId);

        VersionTag saved = versionRepository.save(tag);
        log.info("Version tag created: id={}, name={}, type={}, by={}", saved.getId(), saved.getTagName(), saved.getResourceType(), userId);

        return VersionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public VersionDiffResponse getVersionDiff(Long versionId) {
        VersionTag tag = versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found: " + versionId));

        int currentCount = getCurrentRowCount(tag.getResourceType());
        int snapshotCount = countSnapshotRows(tag.getSnapshotData());
        String preview = tag.getSnapshotData().length() > 500
                ? tag.getSnapshotData().substring(0, 500) + "..."
                : tag.getSnapshotData();

        return new VersionDiffResponse(tag.getId(), tag.getTagName(), tag.getResourceType(), snapshotCount, currentCount, preview);
    }

    @Transactional
    public void rollback(Long versionId, Long userId) {
        VersionTag tag = versionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Version not found: " + versionId));

        try {
            switch (tag.getResourceType()) {
                case "functions" -> rollbackFunctions(tag.getSnapshotData());
                case "menus" -> rollbackMenus(tag.getSnapshotData());
                case "message-resources" -> rollbackMessageResources(tag.getSnapshotData());
                default -> throw new BadRequestException("Unknown resource type: " + tag.getResourceType());
            }
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to parse snapshot data");
        }

        historyService.recordChange(tag.getResourceType(), 0L, "ROLLBACK", null, null, "Rolled back to: " + tag.getTagName(), userId);
        log.info("Rollback completed: versionId={}, tag={}, type={}, by={}", versionId, tag.getTagName(), tag.getResourceType(), userId);
    }

    private String createSnapshot(String resourceType) {
        try {
            return switch (resourceType) {
                case "functions" -> objectMapper.writeValueAsString(functionRepository.findAllByOrderByRowOrderAsc());
                case "menus" -> objectMapper.writeValueAsString(menuRepository.findAllByOrderByRowOrderAsc());
                case "message-resources" -> objectMapper.writeValueAsString(messageResourceRepository.findAllByOrderByRowOrderAsc());
                default -> throw new BadRequestException("Unknown resource type: " + resourceType);
            };
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Failed to create snapshot");
        }
    }

    private void rollbackFunctions(String snapshotData) throws JsonProcessingException {
        functionRepository.deleteAll();
        List<FunctionResource> entities = objectMapper.readValue(snapshotData,
                objectMapper.getTypeFactory().constructCollectionType(List.class, FunctionResource.class));
        entities.forEach(e -> e.setId(null));
        functionRepository.saveAll(entities);
    }

    private void rollbackMenus(String snapshotData) throws JsonProcessingException {
        menuRepository.deleteAll();
        List<MenuResource> entities = objectMapper.readValue(snapshotData,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MenuResource.class));
        entities.forEach(e -> e.setId(null));
        menuRepository.saveAll(entities);
    }

    private void rollbackMessageResources(String snapshotData) throws JsonProcessingException {
        messageResourceRepository.deleteAll();
        List<MessageResource> entities = objectMapper.readValue(snapshotData,
                objectMapper.getTypeFactory().constructCollectionType(List.class, MessageResource.class));
        entities.forEach(e -> e.setId(null));
        messageResourceRepository.saveAll(entities);
    }

    private int getCurrentRowCount(String resourceType) {
        return switch (resourceType) {
            case "functions" -> (int) functionRepository.count();
            case "menus" -> (int) menuRepository.count();
            case "message-resources" -> (int) messageResourceRepository.count();
            default -> 0;
        };
    }

    private int countSnapshotRows(String snapshotData) {
        try {
            return objectMapper.readTree(snapshotData).size();
        } catch (JsonProcessingException e) {
            return 0;
        }
    }
}
