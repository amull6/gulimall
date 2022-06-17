package com.wz.gulimall.search.thread;

import java.util.concurrent.*;

public class CombineTest {
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

//        Future<Void> future03 = future01.runAfterBothAsync(future02, () -> {
//            System.out.println("线程3：" + 2);
//        }, executorService);

//        Future<Void> future04 = future01.thenAcceptBothAsync(future02, (f1,f2) -> {
//            System.out.println("线程3：" + 2*f1*f2);
//        }, executorService);

        Future<Integer> future05 = future01.thenCombineAsync(future02, (f1, f2) -> {
            System.out.println("线程3：" + 2 * f1 * f2);
            return 2 * f1 * f2;
        }, executorService);
        System.out.println(future05.get());
    }
}

