package com.jackpang.exceptions;

/**
 * description: NetworkException
 * date: 11/4/23 6:08 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class SerializeException extends RuntimeException {
    public SerializeException(Throwable cause) {
        super(cause);
    }

    public SerializeException() {
    }

    public SerializeException(String message) {
        super(message);
    }
}
