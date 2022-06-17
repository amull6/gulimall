package com.wz.gulimall.search.thread;

import java.util.concurrent.*;

public class EithorTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("线程1：" + 5);
            return 5;
        }, executorService);

        CompletableFuture<Integer> future02 = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("线程2：" + 3);
            return 3;
        }, executorService);

//        Future<Void> future03 = future01.runAfterEitherAsync(future02, () -> {
//            System.out.println("线程3：" + 2);
//        }, executorService);

//        Future<Void> future03 = future01.acceptEitherAsync(future02, (f) -> {
//            System.out.println("线程3：" + f);
//        }, executorService);

        Future<Integer> future03 = future01.applyToEitherAsync(future02, (f) -> {
            System.out.println("线程3：" + f);
            return f * 2;
        }, executorService);
        System.out.println(future03.get());
    }
}

