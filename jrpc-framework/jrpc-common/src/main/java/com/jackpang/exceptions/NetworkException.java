package com.jackpang.exceptions;

import java.net.SocketException;

/**
 * description: NetworkException
 * date: 11/4/23 6:08 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class NetworkException extends RuntimeException {
    public NetworkException(Throwable cause) {
        super(cause);
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException() {
    }
}
