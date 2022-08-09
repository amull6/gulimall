package com.wz.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.rabbitmq.client.Channel;
import com.wz.common.constant.OrderConstant;
import com.wz.common.to.SkuHasStockVo;
import com.wz.common.utils.R;
import com.wz.common.vo.MemberResVo;
import com.wz.gulimall.order.entity.OrderItemEntity;
import com.wz.gulimall.order.exception.NoStockException;
import com.wz.gulimall.order.feign.CartFeignService;
import com.wz.gulimall.order.feign.MemberFeignClient;
import com.wz.gulimall.order.feign.ProductFeignService;
import com.wz.gulimall.order.feign.WareFeignService;
import com.wz.gulimall.order.interceptor.LoginUserInterceptor;
import com.wz.gulimall.order.service.OrderItemService;
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

    public ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    //创建订单
    private OrderCreateTo orderCreate() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
//            生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity orderEntity = buildOrder(orderSn);

//        设置订单项
//            处理订单商品信息
//            获取购物车中的商品信息
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);

//        计算价格及优惠
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
//        设置删除状态
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
//        订单信息


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
//        优惠信息
//        积分
        orderItemEntity.setGiftGrowth(obj.getPrice().multiply(new BigDecimal(obj.getCount())).intValue());
        orderItemEntity.setGiftIntegration(obj.getPrice().multiply(new BigDecimal(obj.getCount())).intValue());
//        orderItemEntity.setCategoryId(obj.get);
//        价格信息
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        BigDecimal origin = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
        BigDecimal subtract = origin.subtract(orderItemEntity.getIntegrationAmount()).subtract(orderItemEntity.getPromotionAmount()).subtract(orderItemEntity.getCouponAmount());
        orderItemEntity.setRealAmount(subtract);
        return orderItemEntity;
    }

    /*
     * 创建订单
     */
    private OrderEntity buildOrder(String orderSn) {
        OrderEntity orderEntity = new OrderEntity();
        OrderSubmitVo orderSubmitVo = confirmVoThreadLocal.get();
        //        获取用户信息
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();

        orderEntity.setMemberId(memberResVo.getId());
        orderEntity.setMemberUsername(memberResVo.getUsername());
        orderEntity.setOrderSn(orderSn);
//            处理地址信息
        //            根据attrId查询地址信息
        R r = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fare = r.getData(new TypeReference<FareVo>() {
        });
//        设置地址信息
        MemberAddressVo memberAddressVo = fare.getAddressVo();
        orderEntity.setReceiverName(memberAddressVo.getName());
        orderEntity.setReceiverPhone(memberAddressVo.getPhone());
        orderEntity.setReceiverPostCode(memberAddressVo.getPostCode());
        orderEntity.setReceiverProvince(memberAddressVo.getProvince());
        orderEntity.setReceiverCity(memberAddressVo.getCity());
        orderEntity.setReceiverRegion(memberAddressVo.getRegion());
        orderEntity.setReceiverDetailAddress(memberAddressVo.getDetailAddress());
//        设置运费信息
        orderEntity.setFreightAmount(fare.getFare());
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
//        验证令牌、验价格、锁库存
        String orderToken = orderSubmitVo.getOrderToken();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long code = (Long) redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResVo.getId()), orderToken);
        if (code == 0L) {
            OrderCreateTo orderCreateTo = orderCreate();
//            验证价格
            if (Math.abs((orderSubmitVo.getPayPrice().subtract(orderCreateTo.getOrder().getPayAmount())).doubleValue()) < 0.01) {
//                保存订单
                saveOrder(orderCreateTo);
//                锁定库存
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
//                    订单创建成功后，发送延时消息释放订单
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
//        关闭订单
        OrderEntity orderNow = this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderEntity.getOrderSn()));
        if (orderNow.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity newOrder = new OrderEntity();
            newOrder.setId(orderEntity.getId());
            newOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(newOrder);
//            关闭订单之后发送解锁库存消息
            rabbitTemplate.convertAndSend("stock.event.exchange", "stock.release.order", orderNow);
        }


    }

    @Override
    public PayVo handlePayVo(String orderSn) {
//        根据订单编号获取订单
        OrderEntity order = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        PayVo vo = new PayVo();
        vo.setOut_trade_no(orderSn);
        vo.setTotal_amount((order.getPayAmount().setScale(2, RoundingMode.UP)).toString());
        List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
        OrderItemEntity orderItemEntity = orderItemEntities.get(0);
//        组合VO
        vo.setSubject(orderItemEntity.getSkuName());
        vo.setBody(orderItemEntity.getSkuAttrsVals());
        return vo;
    }

    @Override
    public PageUtils listOrderWithItem(Map<String, Object> params) {
        MemberResVo memberResVo = LoginUserInterceptor.loginUser.get();
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberResVo.getId())
        );
        List<OrderEntity> orderEntities = page.getRecords().stream().map((item) -> {
            List<OrderItemEntity> orderItemEntities = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", item.getOrderSn()));
            item.setOrderItemEntityList(orderItemEntities);
            return item;
        }).collect(Collectors.toList());
        page.setRecords(orderEntities);
        return new PageUtils(page);
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