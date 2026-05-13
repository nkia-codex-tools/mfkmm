package com.mkfmm.deploy.domain.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Deployment {

    private static final DateTimeFormatter VERSION_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private String id;
    private String version;
    private String format;
    private int totalRecords;
    private long fileSize;
    private String filePath;
    private String userId;
    private Instant createdAt;

    public Deployment(String format, int totalRecords, long fileSize, String filePath, String userId) {
        this.version = LocalDateTime.now().format(VERSION_FORMAT);
        this.format = format;
        this.totalRecords = totalRecords;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.userId = userId;
        this.createdAt = Instant.now();
    }

    public Deployment() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public int getTotalRecords() { return totalRecords; }
    public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
