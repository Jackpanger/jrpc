package com.jackpang.loadBalancer.impl;

import com.jackpang.JrpcBootstrap;
import com.jackpang.exceptions.LoadBalancerException;
import com.jackpang.loadBalancer.AbstractLoadBalancer;
import com.jackpang.loadBalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description: RoundRobinLoadBalancer
 * date: 11/21/23 7:48 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimenSelector(serviceList);
    }

    private static class MinimumResponseTimenSelector implements Selector {
        private List<InetSocketAddress> serviceList;
        private AtomicInteger index;

        public MinimumResponseTimenSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = JrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.firstEntry();
            if (entry != null) {
                if (log.isDebugEnabled()) {
                    log.debug("MinimumResponseTimeLoadBalancer select response time: {}ms", entry.getKey());
                }
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }
            // get from the cache
            Channel channel = (Channel) JrpcBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();
        }

    }
}
