package com.jackpang.exceptions;

/**
 * description: NetworkException
 * date: 11/4/23 6:08 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class LoadBalancerException extends RuntimeException {
    public LoadBalancerException(Throwable cause) {
        super(cause);
    }

    public LoadBalancerException() {
    }

    public LoadBalancerException(String message) {
        super(message);
    }
}
