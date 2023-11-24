package com.jackpang.impl;

import com.jackpang.HelloJrpc;
import com.jackpang.annotation.JrpcApi;

/**
 * description: HelloJrpcImpl
 * date: 11/3/23 9:59 PM
 * author: jinhao_pang
 * version: 1.0
 */
@JrpcApi(group = "primary")
public class HelloJrpcImpl implements HelloJrpc {
    @Override
    public String sayHi(String msg) {
        return "Hi, consumer:" + msg + " !";
    }
}
