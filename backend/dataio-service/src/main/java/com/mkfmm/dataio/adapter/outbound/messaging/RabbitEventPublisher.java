package com.mkfmm.dataio.adapter.outbound.messaging;

import com.mkfmm.dataio.application.port.outbound.EventPublisherPort;
import com.mkfmm.shared.event.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);
    private static final String EXCHANGE = "mkfmm.events";

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String routingKey, BaseEvent event) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
            log.info("Event published: {} (eventId={})", routingKey, event.eventId());
        } catch (Exception e) {
            log.error("Failed to publish event: {} (eventId={})", routingKey, event.eventId(), e);
        }
    }
}
