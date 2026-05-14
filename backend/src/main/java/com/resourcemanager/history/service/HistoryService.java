package com.resourcemanager.history.service;

import com.resourcemanager.history.dto.HistoryResponse;
import com.resourcemanager.history.entity.ChangeHistory;
import com.resourcemanager.history.repository.ChangeHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class HistoryService {

    private final ChangeHistoryRepository repository;

    public HistoryService(ChangeHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<HistoryResponse> getHistory(String resourceType, Long changedBy,
                                             Instant from, Instant to, Pageable pageable) {
        return repository.findFiltered(resourceType, changedBy, from, to, pageable)
                .map(HistoryResponse::from);
    }

    @Transactional
    public void recordChange(String resourceType, Long resourceId, String changeType,
                              String fieldName, String oldValue, String newValue, Long userId) {
        ChangeHistory history = new ChangeHistory();
        history.setResourceType(resourceType);
        history.setResourceId(resourceId);
        history.setChangeType(changeType);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedBy(userId);
        repository.save(history);
    }

    @Transactional
    public void recordBatchChanges(List<ChangeHistory> changes) {
        repository.saveAll(changes);
    }
}
