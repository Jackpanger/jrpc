package com.jackpang.exceptions;

/**
 * description: NetworkException
 * date: 11/4/23 6:08 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class CompressException extends RuntimeException {
    public CompressException(Throwable cause) {
        super(cause);
    }

    public CompressException() {
    }

    public CompressException(String message) {
        super(message);
    }
}
