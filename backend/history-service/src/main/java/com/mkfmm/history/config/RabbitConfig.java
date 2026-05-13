package com.mkfmm.history.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "mkfmm.history.events";
    public static final String DLQ = "mkfmm.history.dlq";
    public static final String DLX = "mkfmm.history.dlx";

    @Bean
    public Queue historyEventsQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public DirectExchange historyDlx() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue historyDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding historyDlqBinding() {
        return BindingBuilder.bind(historyDlq()).to(historyDlx()).with("dlq");
    }

    @Bean
    public Binding authEventsBinding() {
        return BindingBuilder.bind(historyEventsQueue())
                .to(new TopicExchange("mkfmm.auth")).with("#");
    }

    @Bean
    public Binding userEventsBinding() {
        return BindingBuilder.bind(historyEventsQueue())
                .to(new TopicExchange("mkfmm.user")).with("#");
    }

    @Bean
    public Binding resourceEventsBinding() {
        return BindingBuilder.bind(historyEventsQueue())
                .to(new TopicExchange("mkfmm.resource")).with("#");
    }

    @Bean
    public Binding dataioEventsBinding() {
        return BindingBuilder.bind(historyEventsQueue())
                .to(new TopicExchange("mkfmm.dataio")).with("#");
    }

    @Bean
    public Binding deployEventsBinding() {
        return BindingBuilder.bind(historyEventsQueue())
                .to(new TopicExchange("mkfmm.deploy")).with("#");
    }

    @Bean
    public MessageConverter historyJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
