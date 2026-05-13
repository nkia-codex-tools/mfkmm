package com.mkfmm.dataio.application.port.inbound;

import com.mkfmm.dataio.domain.model.ExportResult;
import com.mkfmm.dataio.domain.model.FileFormat;

import java.util.Map;

public interface ExportUseCase {
    ExportResult exportPartial(Map<String, String> searchParams, FileFormat format, String userId);
    ExportResult exportAll(FileFormat format, String userId);
}
