package com.mkfmm.dataio.adapter.inbound.controller;

import com.mkfmm.dataio.adapter.inbound.dto.ImportResponse;
import com.mkfmm.dataio.application.port.inbound.ImportUseCase;
import com.mkfmm.dataio.domain.model.ConflictPolicy;
import com.mkfmm.dataio.domain.model.ImportJob;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dataio/import")
public class ImportController {

    private final ImportUseCase importUseCase;

    public ImportController(ImportUseCase importUseCase) {
        this.importUseCase = importUseCase;
    }

    @PostMapping
    public ResponseEntity<ImportResponse> importFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "conflictPolicy", defaultValue = "SKIP") ConflictPolicy conflictPolicy,
            @RequestHeader("X-User-Id") String userId) {

        ImportJob job = importUseCase.importFile(file, conflictPolicy, userId);
        return ResponseEntity.ok(ImportResponse.from(job));
    }
}
