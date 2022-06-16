package com.wz.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletetableTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("开始执行main方法");
        ExecutorService executorService = Executors.newFixedThreadPool(5);
//        CompletableFuture.runAsync(() -> {
//            System.out.println("线程：" + Thread.currentThread().getId());
//            System.out.println(10 / 5);
//        }, executorService);
//        感知
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("线程：" + Thread.currentThread().getId());
//            int a = 10 / 5;
//            System.out.println(a);
//            return a;
//        }, executorService).whenComplete((res, exp) -> {
//            System.out.println("我的结果" + res);
//            System.out.println("我的异常" + exp);
//        }).exceptionally((exp) -> {
//            return 10;
//        });
//        感知并修改
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程：" + Thread.currentThread().getId());
            int a = 10 / 5;
            System.out.println(a);
            return a;
        }, executorService).whenComplete((res, exp) -> {
            System.out.println("我的结果" + res);
            System.out.println("我的异常" + exp);
        }).handle((res, exp)->{
            if (res != null) {
                return res * 2;
            }
            if (exp != null) {
                return 0;
            }
            return 0;
        }).exceptionally((exp) -> {
            return 10;
        });
        System.out.println("主线程获取结果：" + future.get());
        System.out.println("结束执行main方法");

    }
}
