package com.wz.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.wz.common.to.mq.SecKillTo;
import com.wz.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Slf4j
@RabbitListener(queues = {"order.seckill.order.queue"})
public class OrderSeckillListener {
    @Autowired
    OrderService orderService;

    @RabbitHandler
    public void seckillOrderCreate(SecKillTo secKillTo, Message message, Channel channel) throws IOException {
        try {
            log.info("准备创建秒杀单的详细信息：" + secKillTo);
            orderService.createSeckillOrder(secKillTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}
