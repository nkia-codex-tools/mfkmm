package com.resourcemanager.resource.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "functions")
public class FunctionResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "a_class") private String aClass;
    @Column(name = "b_class") private String bClass;
    @Column(name = "c_class") private String cClass;
    private String action;
    @Column(name = "function_name") private String functionName;
    @Column(name = "function_id", nullable = false) private String functionId;
    private String type;
    @Column(nullable = false) private Boolean light = false;
    @Column(nullable = false) private Boolean standard = false;
    @Column(nullable = false) private Boolean enterprise = false;
    @Column(name = "system_menu", nullable = false) private Boolean systemMenu = false;
    @Column(name = "product_domain") private String productDomain;
    @Column(name = "domain_license_resource_type") private String domainLicenseResourceType;
    @Column(name = "related_services") private String relatedServices;
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
    public String getAClass() { return aClass; }
    public void setAClass(String aClass) { this.aClass = aClass; }
    public String getBClass() { return bClass; }
    public void setBClass(String bClass) { this.bClass = bClass; }
    public String getCClass() { return cClass; }
    public void setCClass(String cClass) { this.cClass = cClass; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }
    public String getFunctionId() { return functionId; }
    public void setFunctionId(String functionId) { this.functionId = functionId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Boolean getLight() { return light; }
    public void setLight(Boolean light) { this.light = light; }
    public Boolean getStandard() { return standard; }
    public void setStandard(Boolean standard) { this.standard = standard; }
    public Boolean getEnterprise() { return enterprise; }
    public void setEnterprise(Boolean enterprise) { this.enterprise = enterprise; }
    public Boolean getSystemMenu() { return systemMenu; }
    public void setSystemMenu(Boolean systemMenu) { this.systemMenu = systemMenu; }
    public String getProductDomain() { return productDomain; }
    public void setProductDomain(String productDomain) { this.productDomain = productDomain; }
    public String getDomainLicenseResourceType() { return domainLicenseResourceType; }
    public void setDomainLicenseResourceType(String domainLicenseResourceType) { this.domainLicenseResourceType = domainLicenseResourceType; }
    public String getRelatedServices() { return relatedServices; }
    public void setRelatedServices(String relatedServices) { this.relatedServices = relatedServices; }
    public Integer getRowOrder() { return rowOrder; }
    public void setRowOrder(Integer rowOrder) { this.rowOrder = rowOrder; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
