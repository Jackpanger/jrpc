package com.jackpang.proxy;

import com.jackpang.JrpcBootstrap;
import com.jackpang.ReferenceConfig;
import com.jackpang.discovery.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: ProxyFactory
 * date: 11/24/23 6:33 AM
 * author: jinhao_pang
 * version: 1.0
 */
public class ProxyFactory {

    private static final Map<Class<?>, Object> cache = new ConcurrentHashMap<>();

    public static <T> T getProxy(Class<T> clazz) {
        if (cache.containsKey(clazz)) {
            return (T) cache.get(clazz);
        }
        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(clazz);

        JrpcBootstrap.getInstance()
                .application("first-jrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("hessian")
                .group("primary")
                .reference(reference);


        // get a proxy object
        T t = reference.get();
        cache.put(clazz, t);
        return t;
    }
}
