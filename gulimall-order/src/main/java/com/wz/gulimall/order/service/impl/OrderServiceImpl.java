package com.wz.gulimall.order.service.impl;

import com.wz.gulimall.order.entity.OrderItemEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.order.dao.OrderDao;
import com.wz.gulimall.order.entity.OrderEntity;
import com.wz.gulimall.order.service.OrderService;


@Service("orderService")
@RabbitListener(queues = {"gulimall.Queque"})
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @RabbitHandler
    public void receiveMessage(OrderEntity orderEntity) {
        System.out.println(orderEntity.getId());
    }

    @RabbitHandler
    public void receiveMessage01(OrderItemEntity orderItemEntity) {
        System.out.println(orderItemEntity.getOrderId());
    }
}