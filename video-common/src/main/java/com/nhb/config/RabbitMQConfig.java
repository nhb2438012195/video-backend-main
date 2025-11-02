package com.nhb.config;

import com.nhb.properties.VideoProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration

public class RabbitMQConfig {
    @Value("${video.Exchange}")
    public  String VIDEO_PROCESS_EXCHANGE ;
    // 视频处理队列
    @Value("${video.transcodeQueue}")
    public  String VIDEO_PROCESS_QUEUE ;
    @Value("${video.transcodeRoutingKey}")
    public  String VIDEO_PROCESS_ROUTING_KEY;
    //视频上传队列
    @Value("${video.uploadQueue}")
    public  String VIDEO_UPLOAD_QUEUE ;
    @Value("${video.uploadRoutingKey}")
    public  String VIDEO_UPLOAD_ROUTING_KEY ;
    @Bean
    public DirectExchange videoProcessExchange() {
        return new DirectExchange(VIDEO_PROCESS_EXCHANGE);
    }
// 视频处理队列
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
// 视频上传队列
    @Bean
    public Queue videoUploadQueue() {
        return new Queue(VIDEO_UPLOAD_QUEUE, true); // durable=true
    }

    @Bean
    public Binding bindingUploadQueue() {
        return BindingBuilder.bind(videoUploadQueue())
                .to(videoProcessExchange())
                .with(VIDEO_UPLOAD_ROUTING_KEY);
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