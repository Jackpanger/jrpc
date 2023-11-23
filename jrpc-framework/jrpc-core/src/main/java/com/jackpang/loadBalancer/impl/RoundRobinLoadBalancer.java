package com.jackpang.loadBalancer.impl;

import com.jackpang.JrpcBootstrap;
import com.jackpang.discovery.Registry;
import com.jackpang.exceptions.LoadBalancerException;
import com.jackpang.loadBalancer.AbstractLoadBalancer;
import com.jackpang.loadBalancer.LoadBalancer;
import com.jackpang.loadBalancer.Selector;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description: RoundRobinLoadBalancer
 * date: 11/21/23 7:48 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements Selector {
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public RoundRobinSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            if (serviceList == null || serviceList.isEmpty()) {
                log.error("No available service in load balancer");
                throw new LoadBalancerException("No available service");
            }
            return serviceList.get(index.getAndIncrement() % serviceList.size());
        }

    }
}
