package com.jackpang.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * description: ShutdownHolder
 * date: 11/24/23 5:10 AM
 * author: jinhao_pang
 * version: 1.0
 */
public class ShutdownHolder {

    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
