package com.mkfmm.user.application.port.outbound;

import com.mkfmm.shared.event.BaseEvent;

public interface EventPublisherPort {
    void publish(BaseEvent event, String routingKey);
}
