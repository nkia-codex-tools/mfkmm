package com.mkfmm.resource.application.port.outbound;

import com.mkfmm.shared.event.BaseEvent;

public interface EventPublisherPort {
    void publish(BaseEvent event, String routingKey);
    void publishAsync(BaseEvent event, String routingKey);
}
