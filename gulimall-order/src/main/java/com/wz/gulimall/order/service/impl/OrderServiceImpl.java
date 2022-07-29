package com.wz.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.rabbitmq.client.Channel;
import com.wz.common.constant.OrderConstant;
import com.wz.common.to.SkuHasStockVo;
import com.wz.common.utils.R;
import com.wz.common.vo.MemberResVo;
import com.wz.gulimall.order.feign.CartFeignService;
import com.wz.gulimall.order.feign.MemberFeignClient;
import com.wz.gulimall.order.feign.WareFeignService;
import com.wz.gulimall.order.interceptor.LoginUserInterceptor;
import com.wz.gulimall.order.vo.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

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


    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
//        获取用户信息
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
//        验证令牌、验价格、锁库存
        String orderToken = orderSubmitVo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long code = (Long) redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()), orderToken);

        return null;
    }

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
        }, executorService);
        CompletableFuture<Void> castItemFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //        查询购物项
            List<OrderItemVo> orderItemVos = cartFeignService.getCastItem();
//            List<OrderItemVo> orderItemVos = new ArrayList<>();
            orderConfirmVo.setItems(orderItemVos);
        }, executorService).thenRunAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> orderItemVos = orderConfirmVo.getItems();
            List<Long> skuIds = orderItemVos.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.hasStock(skuIds);
            List<SkuHasStockVo> skuHasStockVos = r.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            Map<Long, Boolean> map = skuHasStockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            orderConfirmVo.setStocks(map);
        }, executorService);
//        用户积分
        orderConfirmVo.setIntegration(memberResVo.getIntegration());
//        生成防重令牌
        String orderToken = OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId();
        redisTemplate.opsForValue().set(orderToken, UUID.randomUUID().toString().replace("-", ""));
        orderConfirmVo.setOrderToken(orderToken);
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