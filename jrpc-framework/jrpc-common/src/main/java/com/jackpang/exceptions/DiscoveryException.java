package com.jackpang.exceptions;

/**
 * description: NetworkException
 * date: 11/4/23 6:08 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class DiscoveryException extends RuntimeException {
    public DiscoveryException(Throwable cause) {
        super(cause);
    }

    public DiscoveryException() {
    }

    public DiscoveryException(String message) {
        super(message);
    }
}
