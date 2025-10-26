package com.nhb.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String VIDEO_PROCESS_EXCHANGE = "video.process.exchange";
    public static final String VIDEO_PROCESS_QUEUE = "video.process.queue";
    public static final String VIDEO_PROCESS_ROUTING_KEY = "video.process";

    @Bean
    public DirectExchange videoProcessExchange() {
        return new DirectExchange(VIDEO_PROCESS_EXCHANGE);
    }

    @Bean
    public Queue videoProcessQueue() {
        return new Queue(VIDEO_PROCESS_QUEUE, true); // durable=true
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(videoProcessQueue())
                .to(videoProcessExchange())
                .with(VIDEO_PROCESS_ROUTING_KEY);
    }
}