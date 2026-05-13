package com.mkfmm.resource.adapter.inbound.controller;

import com.mkfmm.resource.adapter.inbound.dto.ResourceResponse;
import com.mkfmm.resource.application.port.outbound.ResourceRepository;
import com.mkfmm.resource.application.port.inbound.ResourceUseCase;
import com.mkfmm.resource.domain.model.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/resources")
public class InternalResourceController {

    private final ResourceRepository resourceRepository;
    private final ResourceUseCase resourceUseCase;

    public InternalResourceController(ResourceRepository resourceRepository, ResourceUseCase resourceUseCase) {
        this.resourceRepository = resourceRepository;
        this.resourceUseCase = resourceUseCase;
    }

    @PostMapping("/check-duplicate")
    public ResponseEntity<Map<String, Object>> checkDuplicate(@RequestBody Map<String, String> request) {
        String key = request.get("key");
        if (key == null) key = request.get("resourceKey");
        boolean exists = resourceRepository.existsByResourceKeyAndNotDeleted(key);
        String existingId = null;
        if (exists) {
            existingId = resourceRepository.findByResourceKeyAndNotDeleted(key)
                    .map(Resource::getId).orElse(null);
        }
        return ResponseEntity.ok(Map.of(
                "duplicate", exists,
                "existingId", existingId != null ? existingId : ""));
    }

    @PostMapping
    public ResponseEntity<Void> createResource(@RequestBody Map<String, String> resource,
                                               @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        String resourceKey = resource.getOrDefault("resourceKey", resource.get("key"));
        String resourceType = resource.getOrDefault("resourceType", "MESSAGE_KEY");
        String content = resource.getOrDefault("content", "");
        String description = resource.getOrDefault("description", "");

        resourceUseCase.createResource(resourceKey, resourceType, content, description, userId, "WRITE");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateResource(@PathVariable String id,
                                               @RequestBody Map<String, String> resource,
                                               @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId) {
        resourceUseCase.updateResource(id, resource.get("content"), resource.get("description"), userId, "WRITE");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchResources(@RequestParam Map<String, String> params) {
        String keyword = params.get("keyword");
        var page = resourceRepository.search(keyword, params.get("resourceType"), null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 1000));
        List<Map<String, Object>> result = page.getContent().stream()
                .map(r -> Map.<String, Object>of(
                        "id", r.getId(),
                        "resourceKey", r.getResourceKey(),
                        "resourceType", r.getResourceType().name(),
                        "content", r.getContent()))
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> findAllActive() {
        var page = resourceRepository.search(null, null, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 100000));
        List<Map<String, Object>> result = page.getContent().stream()
                .map(r -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", r.getId());
                    map.put("resourceKey", r.getResourceKey());
                    map.put("resourceType", r.getResourceType().name());
                    map.put("content", r.getContent());
                    map.put("description", r.getDescription() != null ? r.getDescription() : "");
                    map.put("createdBy", r.getCreatedBy());
                    map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
                    return map;
                })
                .toList();
        return ResponseEntity.ok(result);
    }
}
