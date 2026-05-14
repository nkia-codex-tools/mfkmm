package com.resourcemanager.resource.controller;

import com.resourcemanager.resource.dto.*;
import com.resourcemanager.resource.service.MessageResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources/message-resources")
public class MessageResourceController {

    private final MessageResourceService service;

    public MessageResourceController(MessageResourceService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<MessageResourceResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<MessageResourceResponse> create(@Valid @RequestBody MessageResourceRequest request,
                                                           Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResourceResponse> update(@PathVariable Long id,
                                                           @Valid @RequestBody MessageResourceRequest request,
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
}
