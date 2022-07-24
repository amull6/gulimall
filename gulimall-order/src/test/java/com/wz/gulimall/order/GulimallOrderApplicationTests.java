package com.wz.gulimall.order;

import com.wz.gulimall.order.entity.OrderEntity;
import com.wz.gulimall.order.entity.OrderItemEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallOrderApplicationTests {
    @Autowired
    AmqpAdmin amqpAdmin;
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    public void contextLoads() {
        System.out.println(123);
    }

    @Test
    public void testSendMessage() {
////        String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
//        Exchange exchange = new TopicExchange("gulimall.Exchange", true, false, null);
//        amqpAdmin.declareExchange(exchange);
////        String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
//        Queue queue = new Queue("gulimall.Queque2",true,false,false,null);
//        amqpAdmin.declareQueue(queue);
////        String destination, DestinationType destinationType, String exchange, String routingKey, Map<String, Object> arguments
//        Binding binding = new Binding("gulimall.Queque2", Binding.DestinationType.QUEUE,"gulimall.Exchange","#.RoutingKey",null);
//        amqpAdmin.declareBinding(binding);
//        String var1, String var2, Object var3, MessagePostProcessor var4, CorrelationData var5
        for (int i = 0; i < 10; i++) {
            if (i < 10) {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId(1L);
                rabbitTemplate.convertAndSend("gulimall.Exchange", "gulimall.RoutingKey", orderEntity, new CorrelationData(UUID.randomUUID().toString()));
            } else {
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                orderItemEntity.setOrderId(2L);
                rabbitTemplate.convertAndSend("gulimall.Exchange", "gulimall.RoutingKey", orderItemEntity, new CorrelationData(UUID.randomUUID().toString()));
            }

        }
    }


}
