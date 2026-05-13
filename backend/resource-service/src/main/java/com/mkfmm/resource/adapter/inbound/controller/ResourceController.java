package com.mkfmm.resource.adapter.inbound.controller;

import com.mkfmm.resource.adapter.inbound.dto.*;
import com.mkfmm.resource.application.port.inbound.ResourceUseCase;
import com.mkfmm.resource.application.port.inbound.ResourceSearchUseCase;
import com.mkfmm.resource.domain.model.Resource;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceUseCase resourceUseCase;
    private final ResourceSearchUseCase searchUseCase;

    public ResourceController(ResourceUseCase resourceUseCase, ResourceSearchUseCase searchUseCase) {
        this.resourceUseCase = resourceUseCase;
        this.searchUseCase = searchUseCase;
    }

    @PostMapping
    public ResponseEntity<?> createResource(@Valid @RequestBody CreateResourceRequest request,
                                            @RequestHeader("X-User-Id") String requesterId,
                                            @RequestHeader("X-User-Role") String requesterRole) {
        Object result = resourceUseCase.createResource(
                request.resourceKey(), request.resourceType(), request.content(),
                request.description(), requesterId, requesterRole);

        if (result instanceof Resource saved) {
            return ResponseEntity.status(HttpStatus.CREATED).body(ResourceResponse.from(saved));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/confirm-choice")
    public ResponseEntity<?> confirmSimilarityChoice(@Valid @RequestBody SimilarityChoiceRequest request,
                                                     @RequestHeader("X-User-Id") String requesterId,
                                                     @RequestHeader("X-User-Role") String requesterRole) {
        Object result = resourceUseCase.confirmSimilarityChoice(
                request.resourceKey(), request.resourceType(), request.content(),
                request.description(), request.chosenAction(), request.chosenResourceId(),
                request.reason(), requesterId, requesterRole);

        if (result instanceof Resource saved) {
            return ResponseEntity.status(HttpStatus.CREATED).body(ResourceResponse.from(saved));
        }
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> updateResource(@PathVariable String id,
                                                            @Valid @RequestBody UpdateResourceRequest request,
                                                            @RequestHeader("X-User-Id") String requesterId,
                                                            @RequestHeader("X-User-Role") String requesterRole) {
        Resource resource = resourceUseCase.updateResource(id, request.content(), request.description(), requesterId, requesterRole);
        return ResponseEntity.ok(ResourceResponse.from(resource));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable String id,
                                               @RequestHeader("X-User-Id") String requesterId,
                                               @RequestHeader("X-User-Role") String requesterRole) {
        resourceUseCase.deleteResource(id, requesterId, requesterRole);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getResource(@PathVariable String id) {
        Resource resource = resourceUseCase.getResourceById(id);
        return ResponseEntity.ok(ResourceResponse.from(resource));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ResourceResponse>> searchResources(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Pageable pageable,
            @RequestHeader("X-User-Id") String requesterId) {
        Page<Resource> results = searchUseCase.search(keyword, resourceType, createdBy, startDate, endDate, pageable, requesterId);
        return ResponseEntity.ok(results.map(ResourceResponse::from));
    }
}
