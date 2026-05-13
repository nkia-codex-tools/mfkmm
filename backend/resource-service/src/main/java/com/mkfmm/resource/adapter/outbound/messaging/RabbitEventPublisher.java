package com.mkfmm.resource.adapter.outbound.messaging;

import com.mkfmm.shared.event.BaseEvent;
import com.mkfmm.resource.application.port.outbound.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventPublisher implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);
    private static final String EXCHANGE = "mkfmm.resource";

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(BaseEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
            log.info("Published event: {} with key: {}", event.eventType(), routingKey);
        } catch (Exception e) {
            log.error("Failed to publish event: {} - {}", event.eventType(), e.getMessage());
            throw e;
        }
    }

    @Async
    @Override
    public void publishAsync(BaseEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event);
            log.info("Published async event: {} with key: {}", event.eventType(), routingKey);
        } catch (Exception e) {
            log.warn("Failed to publish async event (non-critical): {} - {}", event.eventType(), e.getMessage());
        }
    }
}
