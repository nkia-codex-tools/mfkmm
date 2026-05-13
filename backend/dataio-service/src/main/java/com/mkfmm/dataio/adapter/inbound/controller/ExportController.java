package com.mkfmm.dataio.adapter.inbound.controller;

import com.mkfmm.dataio.application.port.inbound.ExportUseCase;
import com.mkfmm.dataio.domain.model.ExportResult;
import com.mkfmm.dataio.domain.model.FileFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/dataio/export")
public class ExportController {

    private final ExportUseCase exportUseCase;

    public ExportController(ExportUseCase exportUseCase) {
        this.exportUseCase = exportUseCase;
    }

    @PostMapping("/partial")
    public ResponseEntity<byte[]> exportPartial(
            @RequestBody Map<String, String> searchParams,
            @RequestParam("format") FileFormat format,
            @RequestHeader("X-User-Id") String userId) {

        ExportResult result = exportUseCase.exportPartial(searchParams, format, userId);
        return buildFileResponse(result);
    }

    @PostMapping("/all")
    public ResponseEntity<byte[]> exportAll(
            @RequestParam("format") FileFormat format,
            @RequestHeader("X-User-Id") String userId) {

        ExportResult result = exportUseCase.exportAll(format, userId);
        return buildFileResponse(result);
    }

    private ResponseEntity<byte[]> buildFileResponse(ExportResult result) {
        String contentType = switch (result.format()) {
            case EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case TSV -> "text/tab-separated-values";
            case JSON -> "application/json";
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.fileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(result.fileSize())
                .body(result.data());
    }
}
