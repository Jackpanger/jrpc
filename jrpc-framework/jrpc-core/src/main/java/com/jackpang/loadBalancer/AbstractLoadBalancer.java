package com.jackpang.loadBalancer;

import com.jackpang.JrpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: AbstractLoadBalancer
 * date: 11/21/23 8:42 PM
 * author: jinhao_pang
 * version: 1.0
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {
    private Map<String, Selector> cache = new ConcurrentHashMap<>(8);

    @Override
    public InetSocketAddress selectServerAddress(String serviceName) {
        // 1. get the service selector from the cache
        Selector selector = cache.get(serviceName);
        if (selector == null) {
            // 2. if the selector is null, create a new one and put it into the cache
            List<InetSocketAddress> serviceList = JrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry().lookup(serviceName);
            selector = getSelector(serviceList);
            cache.put(serviceName, selector);
        }
        return selector.getNext();
    }

    @Override
    public synchronized void reloadBalance(String serviceName, List<InetSocketAddress> addresses) {
        cache.put(serviceName, getSelector(addresses));
    }


    /**
     * get the selector
     * @param serviceList
     * @return
     */
    protected abstract Selector getSelector(List<InetSocketAddress> serviceList);

}
