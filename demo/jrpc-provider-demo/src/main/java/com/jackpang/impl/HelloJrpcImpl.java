package com.jackpang.impl;

import com.jackpang.HelloJrpc;

/**
 * description: HelloJrpcImpl
 * date: 11/3/23 9:59 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class HelloJrpcImpl implements HelloJrpc {
    @Override
    public String sayHi(String msg) {
        return "Hi, consumer:" + msg + " !";
    }
}
