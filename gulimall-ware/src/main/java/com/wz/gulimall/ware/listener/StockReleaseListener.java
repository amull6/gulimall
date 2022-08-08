package com.wz.gulimall.ware.listener;

import com.rabbitmq.client.Channel;
import com.wz.common.to.OrderTo;
import com.wz.common.to.mq.StockLockedTo;
import com.wz.gulimall.ware.feign.OrderFeignService;
import com.wz.gulimall.ware.service.WareOrderTaskDetailService;
import com.wz.gulimall.ware.service.WareOrderTaskService;
import com.wz.gulimall.ware.service.WareSkuService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@RabbitListener(queues = "stock.release.stock.queue")
@Service
public class StockReleaseListener {
    @Autowired
    WareOrderTaskService wareOrderTaskService;

    @Autowired
    WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    OrderFeignService orderFeignService;

    @Autowired
    WareSkuService wareSkuService;


//    解锁
//    查询工作单
//    有 库存锁定成功
//    根据工作单查询订单
//    没有订单 必须解锁
//    有订单 判断订单的状态 已取消 解锁库存 没取消 不解锁
//    没有 库存锁定失败，无需解锁
    @RabbitHandler
    public void handleStockLockRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        try {
            System.out.println("库存消息过期,查询订单是否消失，准备解锁库存");
            wareSkuService.unLocked(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

    @RabbitHandler
    public void handleStockLockReleaseAfterOrderCLose(OrderTo order, Message message, Channel channel) throws IOException {
        try {
            System.out.println("定时关闭订单，准备解锁库存");
            wareSkuService.unLockedAfterOrderCLose(order);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            e.printStackTrace();
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
}
