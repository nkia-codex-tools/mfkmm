package com.mkfmm.dataio.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ImportJob {

    private String id;
    private String fileName;
    private FileFormat fileFormat;
    private long fileSize;
    private ImportStatus status;
    private ConflictPolicy conflictPolicy;
    private int totalRows;
    private int successCount;
    private int failedCount;
    private int skippedCount;
    private List<ImportError> errors;
    private String userId;
    private Instant createdAt;
    private Instant completedAt;

    public ImportJob(String fileName, FileFormat fileFormat, long fileSize, ConflictPolicy conflictPolicy, String userId) {
        this.fileName = fileName;
        this.fileFormat = fileFormat;
        this.fileSize = fileSize;
        this.status = ImportStatus.PROCESSING;
        this.conflictPolicy = conflictPolicy;
        this.userId = userId;
        this.errors = new ArrayList<>();
        this.createdAt = Instant.now();
    }

    public void complete(int totalRows, int successCount, int failedCount, int skippedCount, List<ImportError> errors) {
        this.totalRows = totalRows;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.skippedCount = skippedCount;
        this.errors = errors;
        this.status = ImportStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void fail() {
        this.status = ImportStatus.FAILED;
        this.completedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileName() { return fileName; }
    public FileFormat getFileFormat() { return fileFormat; }
    public long getFileSize() { return fileSize; }
    public ImportStatus getStatus() { return status; }
    public ConflictPolicy getConflictPolicy() { return conflictPolicy; }
    public int getTotalRows() { return totalRows; }
    public int getSuccessCount() { return successCount; }
    public int getFailedCount() { return failedCount; }
    public int getSkippedCount() { return skippedCount; }
    public List<ImportError> getErrors() { return errors; }
    public String getUserId() { return userId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}
