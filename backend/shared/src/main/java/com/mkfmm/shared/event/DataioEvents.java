package com.mkfmm.shared.event;

public final class DataioEvents {

    private DataioEvents() {}

    public static final String IMPORT_COMPLETED = "dataio.import.completed";
    public static final String EXPORT_COMPLETED = "dataio.export.completed";

    public record ImportCompletedPayload(String jobId, String fileName, int totalRows, int successCount, int failedCount, int skippedCount) {}
    public record ExportCompletedPayload(String format, String exportType, int recordCount) {}
}
