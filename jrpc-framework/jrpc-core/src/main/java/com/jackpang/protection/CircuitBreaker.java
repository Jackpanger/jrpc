package com.jackpang.protection;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * description: CircuitBreaker
 * date: 11/24/23 12:48 AM
 * author: jinhao_pang
 * version: 1.0
 */
public class CircuitBreaker {
    // Open Close HalfOpen
    // Get metrics from the service: error rate, response time, etc.

    private volatile boolean isOpen = false;

    // overall request count
    private AtomicInteger requestCount = new AtomicInteger(0);

    // error request count
    private AtomicInteger errorRequest = new AtomicInteger(0);

    // error rate threshold
    private int maxErrorRequest;
    private float maxErrorRate;

    public CircuitBreaker(int maxErrorRequest, float maxErrorRate) {
        this.maxErrorRequest = maxErrorRequest;
        this.maxErrorRate = maxErrorRate;
    }

    // check if the service can be open
    public boolean isBreak() {
        if (isOpen) {
            return true;
        }

        if (errorRequest.get() >= maxErrorRequest) {
            isOpen = true;
            return true;
        }

        if (errorRequest.get() > 0 && requestCount.get() > 0 &&
                errorRequest.get() / (1.0 * requestCount.get()) > maxErrorRate) {
            isOpen = true;
            return true;
        }

        return false;
    }


    // record the request count and error request count
    public void recordRequest() {
        this.requestCount.getAndIncrement();
    }

    public void recordErrorRequest() {
        this.errorRequest.getAndIncrement();
    }

    public void reset() {
        this.isOpen = false;
        this.requestCount.set(0);
        this.errorRequest.set(0);

    }
    public static  void main(String[] args) {
        CircuitBreaker circuitBreaker = new CircuitBreaker(10, 0.5f);
        for (int i = 1; i < 100; i++) {
            circuitBreaker.recordRequest();
            if (i % 10 == 0) {
                circuitBreaker.recordErrorRequest();
            }
            System.out.println(circuitBreaker.isBreak());
        }
    }
}
