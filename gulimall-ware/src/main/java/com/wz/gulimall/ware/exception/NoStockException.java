package com.wz.gulimall.ware.exception;

public class NoStockException extends RuntimeException{
    public NoStockException(Long skuId) {
        super("商品："+skuId+"，库存不足");
    }
}
