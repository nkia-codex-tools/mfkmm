package com.resourcemanager.resource.service;

import com.resourcemanager.resource.dto.*;
import com.resourcemanager.resource.entity.MessageResource;
import com.resourcemanager.resource.repository.MessageResourceRepository;
import com.resourcemanager.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageResourceService {

    private final MessageResourceRepository repository;

    public MessageResourceService(MessageResourceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MessageResourceResponse> getAll() {
        List<MessageResource> all = repository.findAllByOrderByRowOrderAsc();
        return computeAggregateFields(all);
    }

    @Transactional
    public MessageResourceResponse create(MessageResourceRequest request, Long userId) {
        int nextOrder = repository.findMaxRowOrder().orElse(0) + 1;

        MessageResource entity = new MessageResource();
        applyRequest(entity, request);
        entity.setRowOrder(nextOrder);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        repository.save(entity);
        List<MessageResource> all = repository.findAllByOrderByRowOrderAsc();
        return computeAggregateFields(all).stream()
                .filter(r -> r.id().equals(entity.getId()))
                .findFirst().orElseThrow();
    }

    @Transactional
    public MessageResourceResponse update(Long id, MessageResourceRequest request, Long userId) {
        MessageResource entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message resource not found: " + id));

        applyRequest(entity, request);
        entity.setUpdatedBy(userId);
        repository.save(entity);

        List<MessageResource> all = repository.findAllByOrderByRowOrderAsc();
        return computeAggregateFields(all).stream()
                .filter(r -> r.id().equals(id))
                .findFirst().orElseThrow();
    }

    @Transactional
    public void delete(Long id, Long userId) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Message resource not found: " + id);
        }
        repository.deleteById(id);
        reassignRowOrders();
    }

    @Transactional
    public void deleteBatch(List<Long> ids, Long userId) {
        repository.deleteAllByIdInBatch(ids);
        reassignRowOrders();
    }

    @Transactional
    public void reorder(ReorderRequest request, Long userId) {
        List<Long> orderedIds = request.orderedIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            repository.updateRowOrder(orderedIds.get(i), i + 1);
        }
    }

    private List<MessageResourceResponse> computeAggregateFields(List<MessageResource> all) {
        Map<String, Long> resourceKeyCounts = all.stream()
                .filter(e -> e.getResourceKey() != null && !e.getResourceKey().isBlank())
                .collect(Collectors.groupingBy(MessageResource::getResourceKey, Collectors.counting()));

        Map<String, Long> koreanCounts = all.stream()
                .filter(e -> e.getKorean() != null && !e.getKorean().isBlank())
                .collect(Collectors.groupingBy(MessageResource::getKorean, Collectors.counting()));

        Map<String, Long> englishCounts = all.stream()
                .filter(e -> e.getEnglish() != null && !e.getEnglish().isBlank())
                .collect(Collectors.groupingBy(MessageResource::getEnglish, Collectors.counting()));

        Map<String, Long> japaneseCounts = all.stream()
                .filter(e -> e.getJapanese() != null && !e.getJapanese().isBlank())
                .collect(Collectors.groupingBy(MessageResource::getJapanese, Collectors.counting()));

        List<MessageResourceResponse> result = new ArrayList<>();
        int rowNum = 0;

        for (MessageResource e : all) {
            boolean hasRowNumber = e.getModule() != null && !e.getModule().isBlank()
                    && e.getResourceKey() != null && !e.getResourceKey().isBlank();

            if (hasRowNumber) rowNum++;

            String duplicateStatus = "";
            if (hasRowNumber) {
                long korCount = koreanCounts.getOrDefault(e.getKorean(), 0L);
                if (korCount > 1) {
                    duplicateStatus = "중복";
                } else {
                    String key = e.getResourceKey().trim();
                    duplicateStatus = key.equals(key.toLowerCase()) ? "정상" : "대문자";
                }
            }

            String fullKey = hasRowNumber ? e.getModule() + "." + e.getResourceKey() : "";
            int rkCount = hasRowNumber ? resourceKeyCounts.getOrDefault(e.getResourceKey(), 0L).intValue() : 0;
            int kCount = hasRowNumber ? koreanCounts.getOrDefault(e.getKorean(), 0L).intValue() : 0;
            int eCount = hasRowNumber ? englishCounts.getOrDefault(e.getEnglish(), 0L).intValue() : 0;
            int jCount = hasRowNumber ? japaneseCounts.getOrDefault(e.getJapanese(), 0L).intValue() : 0;

            result.add(MessageResourceResponse.from(e, rowNum, duplicateStatus, fullKey, rkCount, kCount, eCount, jCount));
        }

        return result;
    }

    private void applyRequest(MessageResource entity, MessageResourceRequest req) {
        entity.setModule(req.module());
        entity.setResourceKey(req.resourceKey());
        entity.setKorean(req.korean());
        entity.setEnglish(req.english());
        entity.setJapanese(req.japanese());
        entity.setDescription(req.description());
        entity.setRegisteredDate(req.registeredDate());
        entity.setRegisteredBy(req.registeredBy());
    }

    private void reassignRowOrders() {
        List<MessageResource> all = repository.findAllByOrderByRowOrderAsc();
        for (int i = 0; i < all.size(); i++) {
            all.get(i).setRowOrder(i + 1);
        }
        repository.saveAll(all);
    }
}
