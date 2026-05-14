package com.resourcemanager.transfer.service;

import com.resourcemanager.resource.entity.FunctionResource;
import com.resourcemanager.resource.entity.MenuResource;
import com.resourcemanager.resource.entity.MessageResource;
import com.resourcemanager.resource.repository.FunctionRepository;
import com.resourcemanager.resource.repository.MenuRepository;
import com.resourcemanager.resource.repository.MessageResourceRepository;
import com.resourcemanager.transfer.dto.ImportPreviewResponse;
import com.resourcemanager.transfer.dto.ImportResultResponse;
import com.resourcemanager.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class TsvImportService {

    private final FunctionRepository functionRepository;
    private final MenuRepository menuRepository;
    private final MessageResourceRepository messageResourceRepository;

    public TsvImportService(FunctionRepository functionRepository,
                            MenuRepository menuRepository,
                            MessageResourceRepository messageResourceRepository) {
        this.functionRepository = functionRepository;
        this.menuRepository = menuRepository;
        this.messageResourceRepository = messageResourceRepository;
    }

    public ImportPreviewResponse preview(String resourceType, MultipartFile file) {
        try {
            List<String[]> dataRows = parseFile(file.getInputStream(), resourceType);
            List<String> errors = new ArrayList<>();

            List<Map<String, String>> previewData = new ArrayList<>();
            for (int i = 0; i < Math.min(dataRows.size(), 100); i++) {
                Map<String, String> row = new LinkedHashMap<>();
                row.put("row", String.valueOf(i + 1));
                row.put("data", String.join(" | ", dataRows.get(i)));
                previewData.add(row);
            }

            return new ImportPreviewResponse(dataRows.size(), dataRows.size(), 0, errors, previewData);
        } catch (IOException e) {
            throw new BadRequestException("Failed to parse TSV file: " + e.getMessage());
        }
    }

    @Transactional
    public ImportResultResponse apply(String resourceType, MultipartFile file, String conflictStrategy, Long userId) {
        try {
            List<String[]> dataRows = parseFile(file.getInputStream(), resourceType);

            int created = switch (resourceType) {
                case "functions" -> importFunctions(dataRows, userId);
                case "menus" -> importMenus(dataRows, userId);
                case "message-resources" -> importMessageResources(dataRows, userId);
                default -> throw new BadRequestException("Unknown resource type: " + resourceType);
            };

            return new ImportResultResponse(created, 0, 0, 0);
        } catch (IOException e) {
            throw new BadRequestException("Failed to process TSV file: " + e.getMessage());
        }
    }

    private List<String[]> parseFile(InputStream input, String resourceType) throws IOException {
        List<String[]> dataRows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                if (line.startsWith("#")) continue;

                if ("message-resources".equals(resourceType)) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;
                    if (!Character.isDigit(trimmed.charAt(0))) continue;
                }

                String[] cols = line.split("\t", -1);
                dataRows.add(cols);
            }
        }
        return dataRows;
    }

    private int importFunctions(List<String[]> rows, Long userId) {
        int baseOrder = functionRepository.findMaxRowOrder().orElse(0);
        int created = 0;

        for (String[] cols : rows) {
            FunctionResource e = new FunctionResource();
            e.setAClass(col(cols, 0));
            e.setBClass(col(cols, 1));
            e.setCClass(col(cols, 2));
            e.setAction(col(cols, 3));
            e.setFunctionName(col(cols, 4));
            e.setFunctionId(col(cols, 5));
            e.setType(col(cols, 6));
            e.setLight("TRUE".equalsIgnoreCase(col(cols, 7)));
            e.setStandard("TRUE".equalsIgnoreCase(col(cols, 8)));
            e.setEnterprise("TRUE".equalsIgnoreCase(col(cols, 9)));
            e.setSystemMenu("TRUE".equalsIgnoreCase(col(cols, 10)));
            e.setProductDomain(col(cols, 11));
            e.setDomainLicenseResourceType(col(cols, 12));
            e.setRelatedServices(col(cols, 13));
            e.setRowOrder(++baseOrder);
            e.setCreatedBy(userId);
            e.setUpdatedBy(userId);
            functionRepository.save(e);
            created++;
        }
        return created;
    }

    private int importMenus(List<String[]> rows, Long userId) {
        int baseOrder = menuRepository.findMaxRowOrder().orElse(0);
        int created = 0;

        for (String[] cols : rows) {
            MenuResource e = new MenuResource();
            e.setMainMenu(col(cols, 0));
            e.setSubMenuGroup(col(cols, 1));
            e.setSubMenu(col(cols, 2));
            e.setMenuLevel1(col(cols, 3));
            e.setMenuLevel2(col(cols, 4));
            e.setMenuLevel3(col(cols, 5));
            e.setMenuId(col(cols, 6));
            e.setIsMenu("TRUE".equalsIgnoreCase(col(cols, 7)));
            e.setIsSystemMenu("TRUE".equalsIgnoreCase(col(cols, 8)));
            e.setFunctionId(col(cols, 9));
            e.setMenuIcon(col(cols, 11));
            e.setRowOrder(++baseOrder);
            e.setCreatedBy(userId);
            e.setUpdatedBy(userId);
            menuRepository.save(e);
            created++;
        }
        return created;
    }

    private int importMessageResources(List<String[]> rows, Long userId) {
        int baseOrder = messageResourceRepository.findMaxRowOrder().orElse(0);
        int created = 0;

        for (String[] cols : rows) {
            // cols[0] = 번호, cols[1] = 중복/대문자 (skip, computed)
            MessageResource e = new MessageResource();
            e.setModule(col(cols, 2));
            e.setResourceKey(col(cols, 3));
            // cols[4] = full_resource_key (skip, computed)
            e.setKorean(col(cols, 5));
            e.setEnglish(col(cols, 6));
            e.setJapanese(col(cols, 7));
            e.setDescription(col(cols, 8));
            e.setRegisteredDate(col(cols, 9));
            e.setRegisteredBy(col(cols, 10));
            e.setRowOrder(++baseOrder);
            e.setCreatedBy(userId);
            e.setUpdatedBy(userId);
            messageResourceRepository.save(e);
            created++;
        }
        return created;
    }

    private String col(String[] cols, int index) {
        if (index >= cols.length) return "";
        return cols[index] != null ? cols[index].trim() : "";
    }
}
