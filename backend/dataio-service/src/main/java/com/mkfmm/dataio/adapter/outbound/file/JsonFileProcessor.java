package com.mkfmm.dataio.adapter.outbound.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mkfmm.dataio.application.port.outbound.FileProcessor;
import com.mkfmm.dataio.domain.model.ImportRow;
import com.mkfmm.dataio.domain.model.ResourceType;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("jsonFileProcessor")
public class JsonFileProcessor implements FileProcessor {

    private final ObjectMapper objectMapper;

    public JsonFileProcessor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public List<ImportRow> parse(InputStream inputStream) {
        try {
            List<Map<String, String>> data = objectMapper.readValue(inputStream, new TypeReference<>() {});
            List<ImportRow> rows = new ArrayList<>();
            int rowNumber = 1;
            for (Map<String, String> item : data) {
                String resourceTypeStr = item.get("resourceType");
                String key = item.get("key");
                String content = item.get("content");
                String description = item.get("description");

                ResourceType resourceType = parseResourceType(resourceTypeStr);
                rows.add(new ImportRow(rowNumber++, resourceType, key, content, description));
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] generate(List<Map<String, Object>> resources) {
        try {
            return objectMapper.writeValueAsBytes(resources);
        } catch (Exception e) {
            throw new RuntimeException("JSON 생성 실패: " + e.getMessage(), e);
        }
    }

    private ResourceType parseResourceType(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return ResourceType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
