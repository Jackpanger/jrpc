package com.jackpang.exceptions;

/**
 * description: NetworkException
 * date: 11/4/23 6:08 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class SpiException extends RuntimeException {
    public SpiException(Throwable cause) {
        super(cause);
    }

    public SpiException() {
    }


    public SpiException(String message) {
        super(message);
    }
}
