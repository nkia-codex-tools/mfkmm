package com.mkfmm.resource.domain.model;

import java.time.Instant;

public class Resource {
    private String id;
    private String resourceKey;
    private ResourceType resourceType;
    private String content;
    private String description;
    private boolean deleted;
    private Instant deletedAt;
    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;

    public Resource(String resourceKey, ResourceType resourceType, String content,
                    String description, String createdBy) {
        this.resourceKey = resourceKey;
        this.resourceType = resourceType;
        this.content = content;
        this.description = description;
        this.deleted = false;
        this.deletedAt = null;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
        this.updatedBy = createdBy;
        this.updatedAt = Instant.now();
    }

    public void update(String content, String description, String updatedBy) {
        if (content != null) this.content = content;
        if (description != null) this.description = description;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }

    public boolean isActive() {
        return !deleted;
    }

    public static Resource reconstruct(String id, String resourceKey, ResourceType resourceType,
                                        String content, String description, String createdBy,
                                        Instant createdAt, String updatedBy, Instant updatedAt,
                                        boolean deleted, Instant deletedAt) {
        Resource r = new Resource(resourceKey, resourceType, content, description, createdBy);
        r.id = id;
        r.createdAt = createdAt;
        r.updatedBy = updatedBy;
        r.updatedAt = updatedAt;
        r.deleted = deleted;
        r.deletedAt = deletedAt;
        return r;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getResourceKey() { return resourceKey; }
    public ResourceType getResourceType() { return resourceType; }
    public String getContent() { return content; }
    public String getDescription() { return description; }
    public boolean isDeleted() { return deleted; }
    public Instant getDeletedAt() { return deletedAt; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public String getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}
