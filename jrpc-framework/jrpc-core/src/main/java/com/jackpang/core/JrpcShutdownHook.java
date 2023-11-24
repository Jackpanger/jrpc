package com.jackpang.core;

import java.util.concurrent.TimeUnit;

/**
 * description: JrpcShutdownHook
 * date: 11/24/23 5:06 AM
 * author: jinhao_pang
 * version: 1.0
 */
public class JrpcShutdownHook extends Thread {
    @Override
    public void run() {
        // 1. open the baffle (boolean flag, synchronized)
        ShutdownHolder.BAFFLE.set(true);
        long start = System.currentTimeMillis();
        // 2. wait for the request to be processed, count down to 0 CountDownLatch
        while (true){
            if (ShutdownHolder.REQUEST_COUNTER.sum() == 0||
                    System.currentTimeMillis() - start > 10000){
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }


    }
}
