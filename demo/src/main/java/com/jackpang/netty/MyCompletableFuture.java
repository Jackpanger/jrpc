package com.jackpang.netty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * description: MyCompleteableFuture
 * date: 11/4/23 8:42 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class MyCompletableFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            int i = 9;

            completableFuture.complete(i);
        }).start();
        System.out.println(completableFuture.get(3, TimeUnit.SECONDS));
    }
}
