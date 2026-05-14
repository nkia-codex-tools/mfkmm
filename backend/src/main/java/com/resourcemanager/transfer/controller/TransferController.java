package com.resourcemanager.transfer.controller;

import com.resourcemanager.transfer.dto.*;
import com.resourcemanager.transfer.service.TsvExportService;
import com.resourcemanager.transfer.service.TsvImportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    private final TsvExportService exportService;
    private final TsvImportService importService;

    public TransferController(TsvExportService exportService, TsvImportService importService) {
        this.exportService = exportService;
        this.importService = importService;
    }

    @PostMapping("/{resourceType}/export")
    public ResponseEntity<byte[]> export(@PathVariable String resourceType,
                                          @RequestBody(required = false) ExportRequest request) {
        var selectedIds = request != null ? request.selectedIds() : null;

        byte[] data = switch (resourceType) {
            case "functions" -> exportService.exportFunctions(selectedIds);
            case "menus" -> exportService.exportMenus(selectedIds);
            case "message-resources" -> exportService.exportMessageResources(selectedIds);
            default -> throw new IllegalArgumentException("Unknown resource type: " + resourceType);
        };

        String filename = resourceType.replace("-", "_") + ".tsv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/tab-separated-values"))
                .body(data);
    }

    @PostMapping("/{resourceType}/import/preview")
    public ResponseEntity<ImportPreviewResponse> importPreview(@PathVariable String resourceType,
                                                               @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(importService.preview(resourceType, file));
    }

    @PostMapping("/{resourceType}/import/apply")
    public ResponseEntity<ImportResultResponse> importApply(@PathVariable String resourceType,
                                                            @RequestParam("file") MultipartFile file,
                                                            @RequestParam String conflictStrategy,
                                                            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(importService.apply(resourceType, file, conflictStrategy, userId));
    }
}
