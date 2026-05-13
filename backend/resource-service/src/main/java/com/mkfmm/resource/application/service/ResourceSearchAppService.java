package com.mkfmm.resource.application.service;

import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.resource.application.port.inbound.ResourceSearchUseCase;
import com.mkfmm.resource.application.port.outbound.EventPublisherPort;
import com.mkfmm.resource.application.port.outbound.ResourceRepository;
import com.mkfmm.resource.domain.model.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class ResourceSearchAppService implements ResourceSearchUseCase {

    private final ResourceRepository resourceRepository;
    private final EventPublisherPort eventPublisher;

    public ResourceSearchAppService(ResourceRepository resourceRepository, EventPublisherPort eventPublisher) {
        this.resourceRepository = resourceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Page<Resource> search(String keyword, String resourceType, String createdBy,
                                 String startDate, String endDate, Pageable pageable, String requesterId) {
        Instant start = startDate != null ? Instant.parse(startDate) : null;
        Instant end = endDate != null ? Instant.parse(endDate) : null;

        Page<Resource> results = resourceRepository.search(keyword, resourceType, createdBy, start, end, pageable);

        eventPublisher.publishAsync(
                new BaseEvent(UUID.randomUUID().toString(), "ResourceSearched", Instant.now(), requesterId,
                        Map.of("query", keyword != null ? keyword : "", "resultCount", String.valueOf(results.getTotalElements()))),
                "resource.searched");

        return results;
    }
}
