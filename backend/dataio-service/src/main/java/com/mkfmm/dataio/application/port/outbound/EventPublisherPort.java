package com.mkfmm.dataio.application.port.outbound;

import com.mkfmm.shared.event.BaseEvent;

public interface EventPublisherPort {
    void publish(String routingKey, BaseEvent event);
}
