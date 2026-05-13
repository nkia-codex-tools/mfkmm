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

    public void processEvent(BaseEvent event) {
        if (workLogRepository.existsBySourceEvent(event.getEventId())) {
            log.debug("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        WorkLogType type = WorkLogType.fromEventType(event.getEventType());
        WorkLog workLog = new WorkLog(type, event.getUserId(), event.getTimestamp(), event.getEventId());

        Map<String, Object> payload = event.getPayload();
        if (payload != null) {
            workLog.setResourceId(getStr(payload, "resourceId"));
            workLog.setResourceKey(getStr(payload, "resourceKey"));
            workLog.setPreviousValue(getStr(payload, "previousContent"));
            workLog.setNewValue(getStr(payload, "newContent"));
            workLog.setDetails(payload.toString());
        }

        workLogRepository.save(workLog);

        if ("UserDeleted".equals(event.getEventType())) {
            String deletedUserId = getStr(payload, "userId");
            if (deletedUserId != null) {
                long marked = workLogRepository.markForDeletionByUserId(deletedUserId);
                log.info("Marked {} work logs for deletion (userId: {})", marked, deletedUserId);
            }
        }

        log.info("Processed event: {} for user: {}", event.getEventType(), event.getUserId());
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
