package com.mkfmm.history.application.service;

import com.mkfmm.history.application.port.outbound.WorkLogRepository;
import com.mkfmm.history.domain.model.WorkLog;
import com.mkfmm.history.domain.model.WorkLogType;
import com.mkfmm.shared.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EventConsumerService {

    private static final Logger log = LoggerFactory.getLogger(EventConsumerService.class);

    private final WorkLogRepository workLogRepository;

    public EventConsumerService(WorkLogRepository workLogRepository) {
        this.workLogRepository = workLogRepository;
    }

    @SuppressWarnings("unchecked")
    public void processEvent(BaseEvent event) {
        if (workLogRepository.existsBySourceEvent(event.eventId())) {
            log.debug("Duplicate event ignored: {}", event.eventId());
            return;
        }

        WorkLogType type = WorkLogType.fromEventType(event.eventType());
        WorkLog workLog = new WorkLog(type, event.userId(), event.timestamp(), event.eventId());

        Object rawPayload = event.payload();
        if (rawPayload instanceof Map<?, ?> payload) {
            Map<String, Object> map = (Map<String, Object>) payload;
            workLog.setResourceId(getStr(map, "resourceId"));
            workLog.setResourceKey(getStr(map, "resourceKey"));
            workLog.setPreviousValue(getStr(map, "previousContent"));
            workLog.setNewValue(getStr(map, "newContent"));
            workLog.setDetails(map.toString());

            if ("UserDeleted".equals(event.eventType())) {
                String deletedUserId = getStr(map, "userId");
                if (deletedUserId != null) {
                    long marked = workLogRepository.markForDeletionByUserId(deletedUserId);
                    log.info("Marked {} work logs for deletion (userId: {})", marked, deletedUserId);
                }
            }
        }

        workLogRepository.save(workLog);
        log.info("Processed event: {} for user: {}", event.eventType(), event.userId());
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
