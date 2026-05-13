package com.mkfmm.dataio.application.service;

import com.mkfmm.dataio.application.port.inbound.ExportUseCase;
import com.mkfmm.dataio.application.port.outbound.EventPublisherPort;
import com.mkfmm.dataio.application.port.outbound.FileProcessor;
import com.mkfmm.dataio.application.port.outbound.ResourceServicePort;
import com.mkfmm.dataio.domain.model.ExportResult;
import com.mkfmm.dataio.domain.model.FileFormat;
import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.shared.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExportApplicationService implements ExportUseCase {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final FileProcessorFactory fileProcessorFactory;
    private final ResourceServicePort resourceService;
    private final EventPublisherPort eventPublisher;

    public ExportApplicationService(FileProcessorFactory fileProcessorFactory,
                                    ResourceServicePort resourceService,
                                    EventPublisherPort eventPublisher) {
        this.fileProcessorFactory = fileProcessorFactory;
        this.resourceService = resourceService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ExportResult exportPartial(Map<String, String> searchParams, FileFormat format, String userId) {
        List<Map<String, Object>> resources = resourceService.searchResources(searchParams);
        if (resources.isEmpty()) {
            throw new BusinessException("DATAIO_007", "내보낼 데이터가 없습니다");
        }

        byte[] data = generateFile(resources, format);
        String fileName = "export_partial_" + LocalDateTime.now().format(FILE_TIMESTAMP) + getExtension(format);

        publishExportEvent(userId, format, resources.size(), "PARTIAL");

        return new ExportResult(fileName, format, data.length, resources.size(), data, Instant.now());
    }

    @Override
    public ExportResult exportAll(FileFormat format, String userId) {
        List<Map<String, Object>> resources = resourceService.findAllActive();
        if (resources.isEmpty()) {
            throw new BusinessException("DATAIO_007", "내보낼 데이터가 없습니다");
        }

        byte[] data = generateFile(resources, format);
        String fileName = "export_all_" + LocalDateTime.now().format(FILE_TIMESTAMP) + getExtension(format);

        publishExportEvent(userId, format, resources.size(), "ALL");

        return new ExportResult(fileName, format, data.length, resources.size(), data, Instant.now());
    }

    private byte[] generateFile(List<Map<String, Object>> resources, FileFormat format) {
        FileProcessor processor = fileProcessorFactory.getProcessor(format);
        return processor.generate(resources);
    }

    private String getExtension(FileFormat format) {
        return switch (format) {
            case EXCEL -> ".xlsx";
            case TSV -> ".tsv";
            case JSON -> ".json";
        };
    }

    private void publishExportEvent(String userId, FileFormat format, int recordCount, String exportType) {
        Map<String, Object> payload = Map.of(
            "format", format.name(),
            "exportType", exportType,
            "recordCount", recordCount
        );
        eventPublisher.publish("dataio.export.completed", BaseEvent.of("dataio.export.completed", userId, payload));
    }
}
