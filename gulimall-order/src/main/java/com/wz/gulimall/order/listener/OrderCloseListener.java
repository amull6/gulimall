package com.wz.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.wz.gulimall.order.entity.OrderEntity;
import com.wz.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderCloseListener {
    @Autowired
    OrderService orderService;

    //    释放订单
    @RabbitHandler
    public void receiveOrder(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        try {
            System.out.println("接收到订单" + message.getBody().toString() + orderEntity.getOrderSn());
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
