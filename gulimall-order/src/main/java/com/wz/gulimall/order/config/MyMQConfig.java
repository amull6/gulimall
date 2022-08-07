package com.wz.gulimall.order.config;

import com.rabbitmq.client.Channel;
import com.wz.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class MyMQConfig {

    @RabbitListener(queues = {"order.release.order.queue"})
    public void receiveOrder(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("接收到订单"+message.getBody().toString()+orderEntity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 60000);
        arguments.put("x-dead-letter-exchange", "order.event.exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
//        String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderRelaseOrderQueue() {
//        String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange() {
//        String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order.event.exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrderBingding() {
//        String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order.event.exchange", "order.create.order", null);
    }

    @Bean
    public Binding orderReleaseOrderBingding() {
//        String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order.event.exchange", "order.release.order", null);
    }


}
