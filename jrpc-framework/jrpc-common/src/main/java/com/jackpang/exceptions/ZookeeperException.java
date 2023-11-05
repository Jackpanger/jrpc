package com.jackpang.exceptions;

/**
 * description: ZookeeperException
 * date: 11/4/23 3:05 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class ZookeeperException extends RuntimeException{
    public ZookeeperException() {
    }

    public ZookeeperException(String message) {
        super(message);
    }
}
