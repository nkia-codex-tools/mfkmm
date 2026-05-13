package com.mkfmm.dataio.application.port.outbound;

import com.mkfmm.dataio.domain.model.ImportRow;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface FileProcessor {
    List<ImportRow> parse(InputStream inputStream);
    byte[] generate(List<Map<String, Object>> resources);
}
