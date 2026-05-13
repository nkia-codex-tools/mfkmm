package com.mkfmm.dataio.application.service;

import com.mkfmm.dataio.application.port.inbound.ImportUseCase;
import com.mkfmm.dataio.application.port.outbound.EventPublisherPort;
import com.mkfmm.dataio.application.port.outbound.FileProcessor;
import com.mkfmm.dataio.application.port.outbound.ImportJobRepository;
import com.mkfmm.dataio.application.port.outbound.ResourceServicePort;
import com.mkfmm.dataio.domain.model.*;
import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ImportApplicationService implements ImportUseCase {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final FileProcessorFactory fileProcessorFactory;
    private final ResourceServicePort resourceService;
    private final ImportJobRepository importJobRepository;
    private final EventPublisherPort eventPublisher;

    public ImportApplicationService(FileProcessorFactory fileProcessorFactory,
                                    ResourceServicePort resourceService,
                                    ImportJobRepository importJobRepository,
                                    EventPublisherPort eventPublisher) {
        this.fileProcessorFactory = fileProcessorFactory;
        this.resourceService = resourceService;
        this.importJobRepository = importJobRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ImportJob importFile(MultipartFile file, ConflictPolicy conflictPolicy, String userId) {
        validateFile(file);

        FileFormat format = resolveFormat(file.getOriginalFilename());
        ImportJob job = new ImportJob(file.getOriginalFilename(), format, file.getSize(), conflictPolicy, userId);
        job = importJobRepository.save(job);

        List<ImportRow> rows;
        try (InputStream is = file.getInputStream()) {
            FileProcessor processor = fileProcessorFactory.getProcessor(format);
            rows = processor.parse(is);
        } catch (Exception e) {
            job.fail();
            importJobRepository.save(job);
            throw new BusinessException("DATAIO_004", "파일 파싱 오류: " + e.getMessage());
        }

        int successCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        List<ImportError> errors = new ArrayList<>();

        for (ImportRow row : rows) {
            String validationError = validateRow(row);
            if (validationError != null) {
                errors.add(new ImportError(row.rowNumber(), "validation", null, validationError));
                failedCount++;
                continue;
            }

            try {
                boolean duplicate = resourceService.checkDuplicate(row.key(), row.resourceType().name());
                if (duplicate) {
                    if (conflictPolicy == ConflictPolicy.SKIP) {
                        skippedCount++;
                    } else {
                        String existingId = resourceService.findExistingId(row.key(), row.resourceType().name());
                        resourceService.updateResource(existingId, toResourceMap(row), userId);
                        successCount++;
                    }
                } else {
                    resourceService.createResource(toResourceMap(row), userId);
                    successCount++;
                }
            } catch (Exception e) {
                errors.add(new ImportError(row.rowNumber(), "resource", row.key(), e.getMessage()));
                failedCount++;
            }
        }

        job.complete(rows.size(), successCount, failedCount, skippedCount, errors);
        job = importJobRepository.save(job);

        publishImportEvent(job, userId);
        return job;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("DATAIO_003", "빈 파일입니다");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("DATAIO_001", "파일 크기가 5MB를 초과합니다");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || resolveFormatOrNull(filename) == null) {
            throw new BusinessException("DATAIO_002", "지원하지 않는 파일 형식입니다 (xlsx, tsv, json만 가능)");
        }
    }

    private FileFormat resolveFormat(String filename) {
        FileFormat format = resolveFormatOrNull(filename);
        if (format == null) {
            throw new BusinessException("DATAIO_002", "지원하지 않는 파일 형식입니다");
        }
        return format;
    }

    private FileFormat resolveFormatOrNull(String filename) {
        if (filename == null) return null;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xlsx")) return FileFormat.EXCEL;
        if (lower.endsWith(".tsv")) return FileFormat.TSV;
        if (lower.endsWith(".json")) return FileFormat.JSON;
        return null;
    }

    private String validateRow(ImportRow row) {
        if (row.resourceType() == null) return "resourceType은 필수입니다";
        if (row.key() == null || row.key().isBlank()) return "key는 필수입니다";
        if (row.content() == null || row.content().isBlank()) return "content는 필수입니다";
        return null;
    }

    private Map<String, String> toResourceMap(ImportRow row) {
        return Map.of(
            "resourceType", row.resourceType().name(),
            "key", row.key(),
            "content", row.content(),
            "description", row.description() != null ? row.description() : ""
        );
    }

    private void publishImportEvent(ImportJob job, String userId) {
        Map<String, Object> payload = Map.of(
            "jobId", job.getId(),
            "fileName", job.getFileName(),
            "totalRows", job.getTotalRows(),
            "successCount", job.getSuccessCount(),
            "failedCount", job.getFailedCount(),
            "skippedCount", job.getSkippedCount()
        );
        eventPublisher.publish("dataio.import.completed", BaseEvent.of("dataio.import.completed", userId, payload));
    }
}
