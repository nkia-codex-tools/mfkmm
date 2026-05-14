package com.resourcemanager.resource.controller;

import com.resourcemanager.resource.dto.*;
import com.resourcemanager.resource.service.FunctionService;
import com.resourcemanager.resource.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources/menus")
public class MenuController {

    private final MenuService service;
    private final FunctionService functionService;

    public MenuController(MenuService service, FunctionService functionService) {
        this.service = service;
        this.functionService = functionService;
    }

    @GetMapping
    public ResponseEntity<List<MenuResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<MenuResponse> create(@Valid @RequestBody MenuRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody MenuRequest request,
                                                Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(service.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<Void> deleteBatch(@RequestBody List<Long> ids, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        service.deleteBatch(ids, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody ReorderRequest request, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        service.reorder(request, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/function-ids")
    public ResponseEntity<List<String>> searchFunctionIds(@RequestParam String query) {
        return ResponseEntity.ok(functionService.searchFunctionIds(query));
    }
}
