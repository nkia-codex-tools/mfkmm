package com.mkfmm.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "mkfmm.events";
    public static final String DLX_EXCHANGE = "mkfmm.events.dlx";
    public static final String AUTH_QUEUE = "history.auth-events";
    public static final String DLQ = "mkfmm.events.dlq";

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue authEventsQueue() {
        return QueueBuilder.durable(AUTH_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue dlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding authEventsBinding(Queue authEventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(authEventsQueue).to(eventsExchange).with("auth.#");
    }

    @Bean
    public Binding dlqBinding(Queue dlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(dlq).to(dlxExchange).with("dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
