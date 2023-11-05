package com.jackpang;

import com.jackpang.discovery.Registry;
import com.jackpang.exceptions.NetworkException;
import com.jackpang.proxy.handler.RpcConsumerInvocationHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * description: ReferenceConfig
 * date: 11/3/23 11:43 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    private Registry registry;

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    /**
     * Use proxy pattern to generate proxy class
     *
     * @return proxy instance
     */
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        InvocationHandler invocationHandler = new RpcConsumerInvocationHandler(registry, interfaceRef);
        // Use JDK dynamic proxy to generate proxy class
        Object helloProxy = Proxy.newProxyInstance(classLoader, classes, invocationHandler);
        return (T) helloProxy;
    }
}
