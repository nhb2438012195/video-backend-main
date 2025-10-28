package com.nhb.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    public static final String VIDEO_PROCESS_EXCHANGE = "video.exchange";
    public static final String VIDEO_PROCESS_QUEUE = "video.transcode.queue";
    public static final String VIDEO_PROCESS_ROUTING_KEY = "video.transcode";

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
    // ====== 配置 JSON 消息转换器 ======
    @Bean
    @Primary  // 如果有多个 MessageConverter，优先使用这个
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ====== 使用 RabbitTemplate 发送消息 ======
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter()); // 使用上面的 converter
        return template;
    }
}