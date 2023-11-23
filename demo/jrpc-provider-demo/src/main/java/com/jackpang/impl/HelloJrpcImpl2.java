package com.jackpang.impl;

import com.jackpang.HelloJrpc2;
import com.jackpang.annotation.JrpcApi;

/**
 * description: HelloJrpcImpl
 * date: 11/3/23 9:59 PM
 * author: jinhao_pang
 * version: 1.0
 */
@JrpcApi
public class HelloJrpcImpl2 implements HelloJrpc2 {
    @Override
    public String sayHi(String msg) {
        return "Hi, consumer222:" + msg + " !";
    }
}
