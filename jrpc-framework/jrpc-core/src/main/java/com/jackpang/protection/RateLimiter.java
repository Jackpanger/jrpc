package com.jackpang.protection;

public interface RateLimiter {
    /**
     * check if the request can be sent
     * @return true if can send request, false if can't send request
     */
    boolean allowRequest();
}
