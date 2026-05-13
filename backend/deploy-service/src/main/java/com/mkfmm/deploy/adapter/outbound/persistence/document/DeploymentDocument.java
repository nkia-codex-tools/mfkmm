package com.mkfmm.deploy.adapter.outbound.persistence.document;

import com.mkfmm.deploy.domain.model.Deployment;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "deployments")
public class DeploymentDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String version;
    private String format;
    private int totalRecords;
    private long fileSize;
    private String filePath;
    private String userId;
    private Instant createdAt;

    public static DeploymentDocument fromDomain(Deployment d) {
        DeploymentDocument doc = new DeploymentDocument();
        doc.id = d.getId();
        doc.version = d.getVersion();
        doc.format = d.getFormat();
        doc.totalRecords = d.getTotalRecords();
        doc.fileSize = d.getFileSize();
        doc.filePath = d.getFilePath();
        doc.userId = d.getUserId();
        doc.createdAt = d.getCreatedAt();
        return doc;
    }

    public Deployment toDomain() {
        Deployment d = new Deployment();
        d.setId(id);
        d.setVersion(version);
        d.setFormat(format);
        d.setTotalRecords(totalRecords);
        d.setFileSize(fileSize);
        d.setFilePath(filePath);
        d.setUserId(userId);
        d.setCreatedAt(createdAt);
        return d;
    }

    public String getId() { return id; }
}
