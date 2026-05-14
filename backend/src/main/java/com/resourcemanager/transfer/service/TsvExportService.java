package com.resourcemanager.transfer.service;

import com.resourcemanager.resource.entity.FunctionResource;
import com.resourcemanager.resource.entity.MenuResource;
import com.resourcemanager.resource.entity.MessageResource;
import com.resourcemanager.resource.repository.FunctionRepository;
import com.resourcemanager.resource.repository.MenuRepository;
import com.resourcemanager.resource.repository.MessageResourceRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TsvExportService {

    private final FunctionRepository functionRepository;
    private final MenuRepository menuRepository;
    private final MessageResourceRepository messageResourceRepository;

    public TsvExportService(FunctionRepository functionRepository, MenuRepository menuRepository, MessageResourceRepository messageResourceRepository) {
        this.functionRepository = functionRepository;
        this.menuRepository = menuRepository;
        this.messageResourceRepository = messageResourceRepository;
    }

    public byte[] exportFunctions(List<Long> selectedIds) {
        List<FunctionResource> entities = selectedIds == null || selectedIds.isEmpty()
                ? functionRepository.findAllByOrderByRowOrderAsc()
                : functionRepository.findAllById(selectedIds);

        StringBuilder sb = new StringBuilder();
        for (FunctionResource e : entities) {
            String resourceKey = (e.getFunctionId() != null && !e.getFunctionId().isBlank())
                    ? "cmm.fn_" + e.getFunctionId().toLowerCase().replace(".", "_") : "";
            sb.append(String.join("\t",
                    str(e.getAClass()), str(e.getBClass()), str(e.getCClass()),
                    str(e.getAction()), str(e.getFunctionName()), str(e.getFunctionId()),
                    str(e.getType()),
                    e.getLight() ? "TRUE" : "FALSE",
                    e.getStandard() ? "TRUE" : "FALSE",
                    e.getEnterprise() ? "TRUE" : "FALSE",
                    e.getSystemMenu() ? "TRUE" : "FALSE",
                    str(e.getProductDomain()), str(e.getDomainLicenseResourceType()),
                    str(e.getRelatedServices()), resourceKey
            )).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportMenus(List<Long> selectedIds) {
        List<MenuResource> entities = selectedIds == null || selectedIds.isEmpty()
                ? menuRepository.findAllByOrderByRowOrderAsc()
                : menuRepository.findAllById(selectedIds);

        StringBuilder sb = new StringBuilder();
        for (MenuResource e : entities) {
            sb.append(String.join("\t",
                    str(e.getMainMenu()), str(e.getSubMenuGroup()), str(e.getSubMenu()),
                    str(e.getMenuLevel1()), str(e.getMenuLevel2()), str(e.getMenuLevel3()),
                    str(e.getMenuId()),
                    e.getIsMenu() ? "TRUE" : "FALSE",
                    e.getIsSystemMenu() ? "TRUE" : "FALSE",
                    str(e.getFunctionId()), "",
                    str(e.getMenuIcon())
            )).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportMessageResources(List<Long> selectedIds) {
        List<MessageResource> entities = selectedIds == null || selectedIds.isEmpty()
                ? messageResourceRepository.findAllByOrderByRowOrderAsc()
                : messageResourceRepository.findAllById(selectedIds);

        // Compute counts
        Map<String, Long> resourceKeyCounts = entities.stream()
                .filter(e -> e.getResourceKey() != null && !e.getResourceKey().isBlank())
                .collect(Collectors.groupingBy(MessageResource::getResourceKey, Collectors.counting()));
        Map<String, Long> koreanCounts = entities.stream()
                .filter(e -> e.getKorean() != null && !e.getKorean().isBlank())
                .collect(Collectors.groupingBy(MessageResource::getKorean, Collectors.counting()));
        Map<String, Long> englishCounts = entities.stream()
                .filter(e -> e.getEnglish() != null && !e.getEnglish().isBlank())
                .collect(Collectors.groupingBy(MessageResource::getEnglish, Collectors.counting()));
        Map<String, Long> japaneseCounts = entities.stream()
                .filter(e -> e.getJapanese() != null && !e.getJapanese().isBlank())
                .collect(Collectors.groupingBy(MessageResource::getJapanese, Collectors.counting()));

        StringBuilder sb = new StringBuilder();
        // Header line
        sb.append("\t중복/ 대문자\t모듈\tresource_key\tfull_resource_key\t국문(한글)\t영문(영어)\t일문(일본어)\t용어에 대한 설명/사용처\t등록/ 수정일자\t등록자\t리소스키 개수\t국문  개수\t영문  개수\t일문  개수\n");

        int rowNum = 1;
        for (MessageResource e : entities) {
            boolean hasKey = e.getModule() != null && !e.getModule().isBlank()
                    && e.getResourceKey() != null && !e.getResourceKey().isBlank();
            if (!hasKey) continue;

            String duplicateStatus;
            long korCount = koreanCounts.getOrDefault(e.getKorean(), 0L);
            if (korCount > 1) {
                duplicateStatus = "중복";
            } else {
                String key = e.getResourceKey().trim();
                duplicateStatus = key.equals(key.toLowerCase()) ? "정상" : "대문자";
            }
            String fullKey = e.getModule() + "." + e.getResourceKey();
            int rkCount = resourceKeyCounts.getOrDefault(e.getResourceKey(), 0L).intValue();
            int kCount = koreanCounts.getOrDefault(e.getKorean(), 0L).intValue();
            int eCount = englishCounts.getOrDefault(e.getEnglish(), 0L).intValue();
            int jCount = japaneseCounts.getOrDefault(e.getJapanese(), 0L).intValue();

            sb.append(String.join("\t",
                    String.valueOf(rowNum + 1),
                    duplicateStatus,
                    str(e.getModule()), str(e.getResourceKey()), fullKey,
                    str(e.getKorean()), str(e.getEnglish()), str(e.getJapanese()),
                    str(e.getDescription()), str(e.getRegisteredDate()), str(e.getRegisteredBy()),
                    String.valueOf(rkCount), String.valueOf(kCount),
                    String.valueOf(eCount), String.valueOf(jCount)
            )).append("\n");
            rowNum++;
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String str(String value) {
        return value != null ? value : "";
    }
}
