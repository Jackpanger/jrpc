package com.jackpang;

/**
 * description: Constant
 * date: 11/4/23 2:42 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class Constant {
    // zookeeper default address
    public  static  final  String DEFAULT_ZK_CONNECT = "127.0.0.1:2181";
    // zookeeper session timeout
    public static final int  TIME_OUT = 10000;
    // server provider and consumer base path
    public static final String BASE_PROVIDERS_PATH = "/jrpc-metadata/providers";
    public static final String BASE_CONSUMERS_PATH = "/jrpc-metadata/consumers";
}
