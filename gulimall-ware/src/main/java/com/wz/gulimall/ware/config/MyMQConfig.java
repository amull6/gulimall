package com.wz.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class MyMQConfig {
//    @RabbitListener(queues = "stock.release.stock.queue")
//    public void handle() {
//
//    }
    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 90000);
        arguments.put("x-dead-letter-exchange", "stock.event.exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release.order");
//        String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
//        String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock.event.exchange", true, false);
    }

    @Bean
    public Binding stockDelayBinding() {
//        String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock.event.exchange", "stock.locked", null);
    }

    @Bean
    public Binding stockReleaseStockBinding() {
//        String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "stock.event.exchange", "stock.release.#", null);
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
