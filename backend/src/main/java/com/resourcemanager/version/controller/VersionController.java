package com.resourcemanager.version.controller;

import com.resourcemanager.version.dto.*;
import com.resourcemanager.version.service.VersionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
public class VersionController {

    private final VersionService versionService;

    public VersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @GetMapping
    public ResponseEntity<List<VersionResponse>> getVersions(@RequestParam(required = false) String resourceType) {
        return ResponseEntity.ok(versionService.getVersions(resourceType));
    }

    @PostMapping
    public ResponseEntity<VersionResponse> createTag(@Valid @RequestBody VersionTagRequest request,
                                                      Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(versionService.createTag(request, userId));
    }

    @GetMapping("/{id}/diff")
    public ResponseEntity<VersionDiffResponse> getVersionDiff(@PathVariable Long id) {
        return ResponseEntity.ok(versionService.getVersionDiff(id));
    }

    @PostMapping("/{id}/rollback")
    public ResponseEntity<Void> rollback(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        versionService.rollback(id, userId);
        return ResponseEntity.ok().build();
    }
}
