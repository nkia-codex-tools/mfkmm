package com.mkfmm.deploy.adapter.inbound.dto;

import com.mkfmm.deploy.domain.model.Deployment;

import java.time.Instant;

public record DeployResponse(
    String id,
    String version,
    String format,
    int totalRecords,
    long fileSize,
    String userId,
    Instant createdAt
) {
    public static DeployResponse from(Deployment d) {
        return new DeployResponse(
            d.getId(), d.getVersion(), d.getFormat(),
            d.getTotalRecords(), d.getFileSize(),
            d.getUserId(), d.getCreatedAt()
        );
    }
}
