package com.mkfmm.deploy.adapter.inbound.controller;

import com.mkfmm.deploy.adapter.inbound.dto.DeployRequest;
import com.mkfmm.deploy.adapter.inbound.dto.DeployResponse;
import com.mkfmm.deploy.application.port.inbound.DeployUseCase;
import com.mkfmm.deploy.domain.model.Deployment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deploy")
public class DeployController {

    private final DeployUseCase deployUseCase;

    public DeployController(DeployUseCase deployUseCase) {
        this.deployUseCase = deployUseCase;
    }

    @PostMapping
    public ResponseEntity<DeployResponse> deploy(
            @RequestBody DeployRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String userRole) {

        Deployment deployment = deployUseCase.deploy(request.format(), userId, userRole);
        return ResponseEntity.ok(DeployResponse.from(deployment));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<DeployResponse>> getHistory(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestHeader("X-User-Role") String userRole) {

        Page<DeployResponse> history = deployUseCase.getDeployHistory(pageable, userRole)
                .map(DeployResponse::from);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> redownload(
            @PathVariable String id,
            @RequestHeader("X-User-Role") String userRole) {

        byte[] data = deployUseCase.redownload(id, userRole);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"deployment-" + id + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
