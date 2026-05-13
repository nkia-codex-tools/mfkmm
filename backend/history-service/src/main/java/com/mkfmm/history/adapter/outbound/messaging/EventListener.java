package com.mkfmm.history.adapter.outbound.messaging;

import com.mkfmm.history.application.service.EventConsumerService;
import com.mkfmm.shared.event.BaseEvent;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    private final EventConsumerService eventConsumerService;

    public EventListener(EventConsumerService eventConsumerService) {
        this.eventConsumerService = eventConsumerService;
    }

    @RabbitListener(queues = "mkfmm.history.events")
    public void handleEvent(BaseEvent event, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            eventConsumerService.processEvent(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Failed to process event: {} - {}", event.eventId(), e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception nackEx) {
                log.error("Failed to NACK message: {}", nackEx.getMessage());
            }
        }
    }
}
