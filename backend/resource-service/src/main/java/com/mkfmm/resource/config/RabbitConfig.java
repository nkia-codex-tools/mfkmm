package com.mkfmm.resource.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "mkfmm.resource";
    public static final String DLX = "mkfmm.resource.dlx";
    public static final String DLQ = "mkfmm.resource.dlq";

    @Bean
    public TopicExchange resourceExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange resourceDeadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue resourceDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding resourceDlqBinding() {
        return BindingBuilder.bind(resourceDeadLetterQueue()).to(resourceDeadLetterExchange()).with("dlq");
    }

    @Bean
    public MessageConverter resourceJsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
