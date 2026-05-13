package com.mkfmm.dataio.adapter.outbound.file;

import com.mkfmm.dataio.application.port.outbound.FileProcessor;
import com.mkfmm.dataio.domain.model.ImportRow;
import com.mkfmm.dataio.domain.model.ResourceType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("excelFileProcessor")
public class ExcelFileProcessor implements FileProcessor {

    @Override
    public List<ImportRow> parse(InputStream inputStream) {
        List<ImportRow> rows = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String resourceTypeStr = getCellValue(row, 0);
                String key = getCellValue(row, 1);
                String content = getCellValue(row, 2);
                String description = getCellValue(row, 3);

                ResourceType resourceType = parseResourceType(resourceTypeStr);
                rows.add(new ImportRow(i, resourceType, key, content, description));
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel 파싱 실패: " + e.getMessage(), e);
        }
        return rows;
    }

    @Override
    public byte[] generate(List<Map<String, Object>> resources) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Resources");

            Row header = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            String[] headers = {"resourceType", "key", "content", "description", "createdBy", "createdAt"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Map<String, Object> resource : resources) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(String.valueOf(resource.getOrDefault("resourceType", "")));
                row.createCell(1).setCellValue(String.valueOf(resource.getOrDefault("key", "")));
                row.createCell(2).setCellValue(String.valueOf(resource.getOrDefault("content", "")));
                row.createCell(3).setCellValue(String.valueOf(resource.getOrDefault("description", "")));
                row.createCell(4).setCellValue(String.valueOf(resource.getOrDefault("createdBy", "")));
                row.createCell(5).setCellValue(String.valueOf(resource.getOrDefault("createdAt", "")));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel 생성 실패: " + e.getMessage(), e);
        }
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> null;
        };
    }

    private ResourceType parseResourceType(String value) {
        if (value == null) return null;
        try {
            return ResourceType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
