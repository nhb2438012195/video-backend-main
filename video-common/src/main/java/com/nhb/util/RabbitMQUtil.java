package com.nhb.util;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * RabbitMQ 工具服务类（非静态工具类，由 Spring 管理）
 * 
 * 功能：
 * - 声明 Exchange / Queue / Binding（按需）
 * - 发送 JSON 消息（自动序列化）
 * - 支持持久化消息
 * 
 * 使用前提：
 * - 已配置 spring.rabbitmq.*
 * - 建议配合 RabbitMQConfig 使用（也可完全动态声明）
 * 
 * 注意：这不是静态工具类！需通过 @Autowired 注入使用。
 */
@Component
public class RabbitMQUtil {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private RabbitAdmin rabbitAdmin;

    /**
     * 初始化 RabbitAdmin（用于动态声明队列等）
     */
    @PostConstruct
    public void init() {
        // 使用与 RabbitTemplate 相同的 ConnectionFactory
        this.rabbitAdmin = new RabbitAdmin(connectionFactory);
        // 设置消息转换器为 JSON（与 RabbitTemplate 一致）
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    }

    /**
     * 声明一个 Direct Exchange（如果不存在）
     * 
     * @param exchangeName 交换机名称
     * @param durable      是否持久化
     */
    public void declareDirectExchange(String exchangeName, boolean durable) {
        DirectExchange exchange = new DirectExchange(exchangeName, durable, false);
        rabbitAdmin.declareExchange(exchange);
    }

    /**
     * 声明一个持久化队列（如果不存在）
     * 
     * @param queueName 队列名称
     */
    public void declareQueue(String queueName) {
        Queue queue = new Queue(queueName, true, false, false);
        rabbitAdmin.declareQueue(queue);
    }

    /**
     * 绑定队列到 Direct Exchange
     * 
     * @param queueName    队列名
     * @param exchangeName 交换机名
     * @param routingKey   路由键
     */
    public void bindQueueToExchange(String queueName, String exchangeName, String routingKey) {
        Binding binding = BindingBuilder
                .bind(new Queue(queueName, true))
                .to(new DirectExchange(exchangeName, true, false))
                .with(routingKey);
        rabbitAdmin.declareBinding(binding);
    }

    /**
     * 发送 JSON 消息（自动持久化）
     * 
     * @param exchange     交换机
     * @param routingKey   路由键
     * @param message      消息对象（会被序列化为 JSON）
     */
    public void sendJsonMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            // 设置消息为持久化（deliveryMode=2）
            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return msg;
        });
    }

    /**
     * 发送简单字符串消息（持久化）
     */
    public void sendTextMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return msg;
        });
    }
}