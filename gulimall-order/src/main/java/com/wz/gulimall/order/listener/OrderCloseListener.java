package com.wz.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.wz.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderCloseListener {
    public void receiveOrder(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("接收到订单"+message.getBody().toString()+orderEntity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }
}
