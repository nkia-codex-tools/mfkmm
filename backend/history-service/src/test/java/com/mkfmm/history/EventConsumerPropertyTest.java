package com.mkfmm.history;

import com.mkfmm.history.application.port.outbound.WorkLogRepository;
import com.mkfmm.history.application.service.EventConsumerService;
import com.mkfmm.history.domain.model.WorkLogType;
import com.mkfmm.shared.event.BaseEvent;
import net.jqwik.api.*;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.StringLength;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventConsumerPropertyTest {

    @Property
    void duplicateEvents_areIgnored(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String userId) {
        WorkLogRepository repo = mock(WorkLogRepository.class);
        String eventId = UUID.randomUUID().toString();

        when(repo.existsBySourceEvent(eventId)).thenReturn(true);

        EventConsumerService service = new EventConsumerService(repo);
        BaseEvent event = new BaseEvent(eventId, "ResourceCreated", Instant.now(), userId, Map.of());

        service.processEvent(event);

        verify(repo, never()).save(any());
    }

    @Property
    void allSupportedEventTypes_mapToWorkLogType(
            @ForAll("supportedEventTypes") String eventType) {
        WorkLogType result = WorkLogType.fromEventType(eventType);
        assert result != null : "Event type " + eventType + " must map to a WorkLogType";
    }

    @Property
    void newEvent_isSavedExactlyOnce(
            @ForAll @AlphaChars @StringLength(min = 3, max = 10) String userId) {
        WorkLogRepository repo = mock(WorkLogRepository.class);
        String eventId = UUID.randomUUID().toString();

        when(repo.existsBySourceEvent(eventId)).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        EventConsumerService service = new EventConsumerService(repo);
        BaseEvent event = new BaseEvent(eventId, "ResourceCreated", Instant.now(), userId,
                Map.of("resourceId", "r1", "resourceKey", "test.key"));

        service.processEvent(event);

        verify(repo, times(1)).save(any());
    }

    @Provide
    Arbitrary<String> supportedEventTypes() {
        return Arbitraries.of(
                "ResourceCreated", "ResourceUpdated", "ResourceDeleted", "ResourceSearched",
                "ImportCompleted", "ExportCompleted", "DeployCompleted",
                "UserCreated", "UserDeleted", "PermissionChanged",
                "UserLoggedIn", "UserLoggedOut", "AccountLocked", "AccountUnlocked",
                "SimilarityChoiceMade"
        );
    }
}
