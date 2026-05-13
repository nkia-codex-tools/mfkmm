package com.mkfmm.deploy.application.port.outbound;

import com.mkfmm.shared.event.BaseEvent;

public interface EventPublisherPort {
    void publish(String routingKey, BaseEvent event);
}
