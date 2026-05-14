package com.resourcemanager.resource.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "message_resources")
public class MessageResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String module;
    @Column(name = "resource_key") private String resourceKey;
    private String korean;
    private String english;
    private String japanese;
    private String description;
    @Column(name = "registered_date") private String registeredDate;
    @Column(name = "registered_by") private String registeredBy;
    @Column(name = "row_order", nullable = false) private Integer rowOrder;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @Column(name = "updated_by", nullable = false) private Long updatedBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getResourceKey() { return resourceKey; }
    public void setResourceKey(String resourceKey) { this.resourceKey = resourceKey; }
    public String getKorean() { return korean; }
    public void setKorean(String korean) { this.korean = korean; }
    public String getEnglish() { return english; }
    public void setEnglish(String english) { this.english = english; }
    public String getJapanese() { return japanese; }
    public void setJapanese(String japanese) { this.japanese = japanese; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getRegisteredDate() { return registeredDate; }
    public void setRegisteredDate(String registeredDate) { this.registeredDate = registeredDate; }
    public String getRegisteredBy() { return registeredBy; }
    public void setRegisteredBy(String registeredBy) { this.registeredBy = registeredBy; }
    public Integer getRowOrder() { return rowOrder; }
    public void setRowOrder(Integer rowOrder) { this.rowOrder = rowOrder; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
