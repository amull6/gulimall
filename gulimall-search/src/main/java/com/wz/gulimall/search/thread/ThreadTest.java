package com.wz.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("开始执行main方法");
//        Thread01 thread01 = new Thread01();
//        new Thread(thread01).start();
//        new Thread(new Thread02()).start();
//        FutureTask<Integer> futureTask = new FutureTask<Integer>(new Thread03());
//        new Thread(futureTask).start();
//        int a = futureTask.get();
//        System.out.println("主线程获取到" + a);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        Future<Integer> future = executorService.submit(new Thread03());
        System.out.println("主线程获取到" + future.get());
        System.out.println("结束执行main方法");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("线程：" + Thread.currentThread().getId());
            System.out.println(10 / 5);
        }
    }

    public static class Thread02 implements Runnable {
        @Override
        public void run() {
            System.out.println("线程：" + Thread.currentThread().getId());
            System.out.println(10 / 5);
        }
    }

    public static class Thread03 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("线程：" + Thread.currentThread().getId());
            int a = 10 / 5;
            System.out.println(a);
            return a;
        }
    }

}
