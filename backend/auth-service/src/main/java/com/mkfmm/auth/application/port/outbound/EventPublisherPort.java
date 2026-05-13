package com.mkfmm.auth.application.port.outbound;

import com.mkfmm.shared.event.BaseEvent;

public interface EventPublisherPort {
    void publish(BaseEvent event);
}
