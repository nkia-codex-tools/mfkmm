package com.mkfmm.dataio.adapter.outbound.persistence.document;

import com.mkfmm.dataio.domain.model.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "import_jobs")
public class ImportJobDocument {

    @Id
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

    public static ImportJobDocument fromDomain(ImportJob job) {
        ImportJobDocument doc = new ImportJobDocument();
        doc.id = job.getId();
        doc.fileName = job.getFileName();
        doc.fileFormat = job.getFileFormat();
        doc.fileSize = job.getFileSize();
        doc.status = job.getStatus();
        doc.conflictPolicy = job.getConflictPolicy();
        doc.totalRows = job.getTotalRows();
        doc.successCount = job.getSuccessCount();
        doc.failedCount = job.getFailedCount();
        doc.skippedCount = job.getSkippedCount();
        doc.errors = job.getErrors();
        doc.userId = job.getUserId();
        doc.createdAt = job.getCreatedAt();
        doc.completedAt = job.getCompletedAt();
        return doc;
    }

    public ImportJob toDomain() {
        ImportJob job = new ImportJob(fileName, fileFormat, fileSize, conflictPolicy, userId);
        job.setId(id);
        if (status == ImportStatus.COMPLETED) {
            job.complete(totalRows, successCount, failedCount, skippedCount, errors);
        } else if (status == ImportStatus.FAILED) {
            job.fail();
        }
        return job;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
