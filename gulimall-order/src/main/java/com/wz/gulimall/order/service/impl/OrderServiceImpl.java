package com.wz.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.rabbitmq.client.Channel;
import com.wz.common.constant.OrderConstant;
import com.wz.common.to.SkuHasStockVo;
import com.wz.common.to.mq.SecKillTo;
import com.wz.common.utils.R;
import com.wz.common.vo.MemberResVo;
import com.wz.gulimall.order.entity.OrderItemEntity;
import com.wz.gulimall.order.entity.PaymentInfoEntity;
import com.wz.gulimall.order.exception.NoStockException;
import com.wz.gulimall.order.feign.CartFeignService;
import com.wz.gulimall.order.feign.MemberFeignClient;
import com.wz.gulimall.order.feign.ProductFeignService;
import com.wz.gulimall.order.feign.WareFeignService;
import com.wz.gulimall.order.interceptor.LoginUserInterceptor;
import com.wz.gulimall.order.service.OrderItemService;
import com.wz.gulimall.order.service.PaymentInfoService;
import com.wz.gulimall.order.utils.OrderStatusEnum;
import com.wz.gulimall.order.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    PaymentInfoService paymentInfoService;

    @Autowired
    OrderService orderService;

    public ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    //????????????
    private OrderCreateTo orderCreate() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
//            ???????????????
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

//        ???????????????
//            ????????????????????????
//            ?????????????????????????????????
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

//        ?????????????????????
        computePrice(orderEntity, orderItemEntities);

        orderCreateTo.setOrder(orderEntity);
        orderCreateTo.setItems(orderItemEntities);

        return orderCreateTo;
    }

    private void computePrice(OrderEntity orderEntity, List<OrderItemEntity> orderItemEntities) {
        BigDecimal total = new BigDecimal("0");
        BigDecimal integration = new BigDecimal("0");
        BigDecimal promotion = new BigDecimal("0");
        BigDecimal coupon = new BigDecimal("0");
        Integer giftGrowth = 0;
        Integer giftIntegration = 0;
        for (OrderItemEntity orderItemEntity : orderItemEntities) {
            total = total.add(orderItemEntity.getRealAmount());
            integration = integration.add(orderItemEntity.getIntegrationAmount());
            promotion = promotion.add(orderItemEntity.getPromotionAmount());
            coupon = coupon.add(orderItemEntity.getCouponAmount());
            giftGrowth += orderItemEntity.getGiftGrowth();
            giftIntegration += orderItemEntity.getGiftIntegration();
        }
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setTotalAmount(total);
        orderEntity.setCouponAmount(coupon);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setGrowth(giftGrowth);
        orderEntity.setIntegration(giftIntegration);
//        ??????????????????
        orderEntity.setDeleteStatus(0);
    }

    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        List<OrderItemVo> orderItemVos = cartFeignService.getCastItem();
        List<OrderItemEntity> orderItemEntities = orderItemVos.stream().map((obj) -> {
            OrderItemEntity orderItemEntity = orderItemEntity = builderOrderItem(obj);
            orderItemEntity.setOrderSn(orderSn);
            return orderItemEntity;
        }).collect(Collectors.toList());
        return orderItemEntities;
    }

    private OrderItemEntity builderOrderItem(OrderItemVo obj) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
//        ????????????


//        spu
        R r = productFeignService.getSpuInfoBySkuId(obj.getSkuId());
        SpuInfoVo spuInfoVo = r.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
        orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
        orderItemEntity.setSpuId(spuInfoVo.getId());
        orderItemEntity.setSpuName(spuInfoVo.getSpuName());
//        sku
        orderItemEntity.setSkuId(obj.getSkuId());
        orderItemEntity.setSkuName(obj.getTitle());
        orderItemEntity.setSkuPic(obj.getImage());
        orderItemEntity.setSkuPrice(obj.getPrice());
        String skuAttr = StringUtils.collectionToDelimitedString(obj.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);
        orderItemEntity.setSkuQuantity(obj.getCount());
//        ????????????
//        ??????
        orderItemEntity.setGiftGrowth(obj.getPrice().multiply(new BigDecimal(obj.getCount())).intValue());
        orderItemEntity.setGiftIntegration(obj.getPrice().multiply(new BigDecimal(obj.getCount())).intValue());
//        orderItemEntity.setCategoryId(obj.get);
//        ????????????
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal subtract = origin.subtract(orderItemEntity.getIntegrationAmount()).subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getCouponAmount());
        orderItemEntity.setRealAmount(subtract);
        return orderItemEntity;
    }

    /*
     * ????????????
     */
    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        //        ??????????????????
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();

        orderEntity.setMemberId(memberResVo.getId());
        orderEntity.setMemberUsername(memberResVo.getUsername());
        orderEntity.setOrderSn(orderSn);
//            ??????????????????
        //            ??????attrId??????????????????
        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fare = r.getData(new TypeReference<FareVo>() {
        });
//        ??????????????????
        MemberAddressVo memberAddressVo = fare.getAddressVo();
        orderEntity.setReceiverName(memberAddressVo.getName());
        orderEntity.setReceiverPhone(memberAddressVo.getPhone());
        orderEntity.setReceiverPostCode(memberAddressVo.getPostCode());
        orderEntity.setReceiverProvince(memberAddressVo.getProvince());
        orderEntity.setReceiverCity(memberAddressVo.getCity());
        orderEntity.setReceiverRegion(memberAddressVo.getRegion());
        orderEntity.setReceiverDetailAddress(memberAddressVo.getDetailAddress());
//        ??????????????????
        orderEntity.setFreightAmount(fare.getFare());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        return orderEntity;
    }


    @Override
    @Transactional
//    @GlobalTransactional
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        confirmVoThreadLocal.set(orderSubmitVo);
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        submitOrderResponseVo.setCode(0);
//        ????????????????????????????????????
        String orderToken = orderSubmitVo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long code = (Long) redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()), orderToken);
        if (code == 0L) {
            OrderCreateTo orderCreateTo = orderCreate();
//            ????????????
            if (Math.abs((orderSubmitVo.getPayPrice().subtract(orderCreateTo.getOrder().getPayAmount())).doubleValue()) < 0.01) {
//                ????????????
                saveOrder(orderCreateTo);
//                ????????????
                WareLockVo wareLockVo = new WareLockVo();
                wareLockVo.setOrderSn(orderCreateTo.getOrder().getOrderSn());
                List<OrderItemVo> orderItemVos = orderCreateTo.getItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareLockVo.setOrderItemVos(orderItemVos);
                R r = wareFeignService.lockOrder(wareLockVo);
                if (r.getCode() == 0) {
//                    ??????????????????????????????????????????????????????
//                    String var1, String var2, Object var3, MessagePostProcessor var4, CorrelationData var5
                    rabbitTemplate.convertAndSend("order.event.exchange", "order.delay.queue", orderCreateTo.getOrder());
//                    int a = 10 / 0;
                    submitOrderResponseVo.setOrderEntity(orderCreateTo.getOrder());
                    return submitOrderResponseVo;
                } else {
                    throw new NoStockException();
                }
            } else {
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }

        } else {
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }
    }

    @Override
    public OrderEntity getOrderStatus(String orderSn) {
        return this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
//        ????????????
        OrderEntity orderNow = this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderEntity.getOrderSn()));
        if (orderNow.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity newOrder = new OrderEntity();
            newOrder.setId(orderEntity.getId());
            newOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(newOrder);
//            ??????????????????????????????????????????
            rabbitTemplate.convertAndSend("stock.event.exchange", "stock.release.order", orderNow);
        }


    }

    @Override
    public PayVo handlePayVo(String orderSn) {
//        ??????????????????????????????
        OrderEntity order = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        PayVo vo = new PayVo();
        vo.setOut_trade_no(orderSn);
        vo.setTotal_amount((order.getPayAmount().setScale(2, RoundingMode.UP)).toString());
        List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
        OrderItemEntity orderItemEntity = orderItemEntities.get(0);
//        ??????VO
        vo.setSubject(orderItemEntity.getSkuName());
        vo.setBody(orderItemEntity.getSkuAttrsVals());
        return vo;
    }

    @Override
    public PageUtils listOrderWithItem(Map<String, Object> params) {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberResVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> orderEntities = page.getRecords().stream().map((item) -> {
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", item.getOrderSn()));
            item.setOrderItemEntityList(orderItemEntities);
            return item;
        }).collect(Collectors.toList());
        page.setRecords(orderEntities);
        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo payAsyncVo) {
        PaymentInfoEntity paymentInfoEntity = new PaymentInfoEntity();
        paymentInfoEntity.setOrderSn(payAsyncVo.getOut_trade_no());
        paymentInfoEntity.setAlipayTradeNo(payAsyncVo.getTrade_no());
        String trade_status = payAsyncVo.getTrade_status();
        paymentInfoEntity.setPaymentStatus(trade_status);
        paymentInfoEntity.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(paymentInfoEntity);

        if (trade_status.equals("TRADE_SUCCESS") || trade_status.equals("TRADE_FINISHED")) {
            this.updateStatus(payAsyncVo.getOut_trade_no(), OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }


    private void updateStatus(String outTradeNo, Integer code) {
        this.baseMapper.updateStatusByOrderSn(outTradeNo, code);

    }

    private void saveOrder(OrderCreateTo orderCreateTo) {
        OrderEntity order = orderCreateTo.getOrder();
        order.setCreateTime(new Date());
        order.setModifyTime(new Date());
        this.save(order);
        orderItemService.saveBatch(orderCreateTo.getItems());
    }


    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //        ??????????????????
            List<MemberAddressVo> addresses = memberFeignClient.getMemberReceiveAddressByMemberId(memberResVo.getId());
            orderConfirmVo.setAddress(addresses);
        }, executorService);
        CompletableFuture<Void> castItemFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //        ???????????????
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
//        ????????????
        orderConfirmVo.setIntegration(memberResVo.getIntegration());
//        ??????????????????
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
                System.out.println("??????????????????");
                channel.basicAck(deliveryTag, false);
            } else {
                System.out.println("?????????????????????");
                channel.basicNack(deliveryTag, false, true);
            }
        } catch (Exception e) {
            //????????????
        }

    }


    @Override
    public void createSeckillOrder(SecKillTo secKillTo) {
//        ????????????
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(secKillTo.getOrderSn());
        orderEntity.setMemberId(secKillTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = secKillTo.getSeckillPrice().multiply(new BigDecimal("" + secKillTo.getNum()));
        orderEntity.setPayAmount(multiply);
        this.save(orderEntity);
//        ???????????????
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        orderItemEntity.setOrderSn(secKillTo.getOrderSn());
        orderItemEntity.setRealAmount(multiply);
        orderItemEntity.setSkuQuantity(secKillTo.getNum());
        orderItemService.save(orderItemEntity);
    }
}