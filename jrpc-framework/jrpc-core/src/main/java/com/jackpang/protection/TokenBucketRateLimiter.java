package com.jackpang.protection;

/**
 * description: TokenBuketRateLimiter
 * date: 11/23/23 11:32 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class TokenBucketRateLimiter implements RateLimiter {
    // 1. number of tokens, tokens>0 means can request
    private int tokens;
    private final int capacity;

    private final int rate;

    private Long lastTokenTime;

    public TokenBucketRateLimiter(int capacity, int rate) {
        this.capacity = capacity;
        this.rate = rate;
        lastTokenTime = System.currentTimeMillis();
        tokens = capacity;
    }

    /**
     * check if the request can be sent
     * @return true if can send request, false if can't send request
     */
    @Override
    public synchronized boolean allowRequest() {
        // 1. add token to the token bucket
        Long currentTime = System.currentTimeMillis();
        long timeInterval = currentTime - lastTokenTime;
        if (timeInterval >= 1000) {
            int needAddToken = (int) (timeInterval * rate / 1000);
            tokens = Math.min(capacity, tokens + needAddToken);
            lastTokenTime = currentTime;
        }
        // 2. get token from the token bucket
        if (tokens>0){
            tokens--;
            return true;
        }else {
            return false;
        }
    }

    public static void main(String[] args) {
        TokenBucketRateLimiter tokenBucketRateLimiter = new TokenBucketRateLimiter(10, 10);
        for (int i = 0; i < 1000; i++) {
            try {
                Thread.sleep(10);
                System.out.println(tokenBucketRateLimiter.allowRequest());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
