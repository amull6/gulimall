package com.wz.gulimall.search.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllOf {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程1：" + 5);
            return 5;
        }, executorService);

        CompletableFuture<Integer> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程2：" + 3);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return 3;
        }, executorService);

        CompletableFuture<Integer> future03 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }            System.out.println("线程2：" + 3);
            System.out.println("线程2：" + 3);
            return 3;
        }, executorService);

//        CompletableFuture<Void> f = CompletableFuture.allOf(future01, future02, future03);
//        f.get();
//        System.out.println("全部执行完了吗");

        CompletableFuture<Object> f = CompletableFuture.anyOf(future01, future02, future03);
        System.out.println("全部执行完了吗"+f.get());
    }
}
