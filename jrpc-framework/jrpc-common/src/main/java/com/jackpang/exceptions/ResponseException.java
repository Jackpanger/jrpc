package com.jackpang.exceptions;

/**
 * description: ResponseException
 * date: 11/24/23 2:21 AM
 * author: jinhao_pang
 * version: 1.0
 */
public class ResponseException extends RuntimeException {
    private byte code;
    private String msg;

    public ResponseException(byte code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
