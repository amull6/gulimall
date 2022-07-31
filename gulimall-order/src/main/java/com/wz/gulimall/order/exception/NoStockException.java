package com.wz.gulimall.order.exception;

public class NoStockException extends RuntimeException{
    public NoStockException(Long skuId) {
        super("商品："+skuId+"，库存不足");
    }

    public NoStockException() {
        super("商品库存不足");
    }
}
