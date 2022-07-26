package com.wz.gulimall.order.service.impl;

import com.rabbitmq.client.Channel;
import com.wz.common.vo.MemberResVo;
import com.wz.gulimall.order.feign.CartFeignService;
import com.wz.gulimall.order.feign.MemberFeignClient;
import com.wz.gulimall.order.interceptor.LoginUserInterceptor;
import com.wz.gulimall.order.vo.MemberAddressVo;
import com.wz.gulimall.order.vo.OrderConfirmVo;
import com.wz.gulimall.order.vo.OrderItemVo;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wz.common.utils.PageUtils;
import com.wz.common.utils.Query;

import com.wz.gulimall.order.dao.OrderDao;
import com.wz.gulimall.order.entity.OrderEntity;
import com.wz.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
@RabbitListener(queues = {"gulimall.Queque"})
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MemberFeignClient memberFeignClient;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ExecutorService executorService;

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //        查询地址列表
            List<MemberAddressVo> addresses = memberFeignClient.getMemberReceiveAddressByMemberId(memberResVo.getId());
            orderConfirmVo.setAddress(addresses);
        });
        CompletableFuture<Void> castItemFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //        查询购物项
            List<OrderItemVo> orderItemVos = cartFeignService.getCastItem();
            orderConfirmVo.setItems(orderItemVos);
        });
//        用户积分
        orderConfirmVo.setIntegration(memberResVo.getIntegration());
//        防重令牌
        CompletableFuture.allOf(addressCompletableFuture, castItemFuture).get();
        return orderConfirmVo;
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @RabbitHandler
    public void receiveMessage(Message message, OrderEntity orderEntity, Channel channel) {
        System.out.println(orderEntity.getId());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println(message.getMessageProperties().getCorrelationId());
        try {
            if (deliveryTag % 2 == 0) {
                System.out.println("确认收到消息");
                channel.basicAck(deliveryTag, false);
            } else {
                System.out.println("没有接受到消息");
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (Exception e) {
            //网络中断
        }

    }

//    @RabbitHandler
//    public void receiveMessage01(OrderItemEntity orderItemEntity) {
//        System.out.println(orderItemEntity.getOrderId());
//    }
}