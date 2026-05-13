package com.mkfmm.dataio.adapter.outbound.file;

import com.mkfmm.dataio.application.port.outbound.FileProcessor;
import com.mkfmm.dataio.domain.model.ImportRow;
import com.mkfmm.dataio.domain.model.ResourceType;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("tsvFileProcessor")
public class TsvFileProcessor implements FileProcessor {

    @Override
    public List<ImportRow> parse(InputStream inputStream) {
        List<ImportRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.readLine(); // skip header
            String line;
            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\t", -1);
                String resourceTypeStr = parts.length > 0 ? parts[0].trim() : null;
                String key = parts.length > 1 ? parts[1].trim() : null;
                String content = parts.length > 2 ? parts[2].trim() : null;
                String description = parts.length > 3 ? parts[3].trim() : null;

                ResourceType resourceType = parseResourceType(resourceTypeStr);
                rows.add(new ImportRow(rowNumber++, resourceType, key, content, description));
            }
        } catch (IOException e) {
            throw new RuntimeException("TSV 파싱 실패: " + e.getMessage(), e);
        }
        return rows;
    }

    @Override
    public byte[] generate(List<Map<String, Object>> resources) {
        StringBuilder sb = new StringBuilder();
        sb.append("resourceType\tkey\tcontent\tdescription\tcreatedBy\tcreatedAt\n");
        for (Map<String, Object> resource : resources) {
            sb.append(escape(resource.getOrDefault("resourceType", ""))).append('\t');
            sb.append(escape(resource.getOrDefault("key", ""))).append('\t');
            sb.append(escape(resource.getOrDefault("content", ""))).append('\t');
            sb.append(escape(resource.getOrDefault("description", ""))).append('\t');
            sb.append(escape(resource.getOrDefault("createdBy", ""))).append('\t');
            sb.append(escape(resource.getOrDefault("createdAt", ""))).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escape(Object value) {
        if (value == null) return "";
        return String.valueOf(value).replace("\t", "\\t").replace("\n", "\\n");
    }

    private ResourceType parseResourceType(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return ResourceType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
