package com.jackpang.loadBalancer.impl;

import com.jackpang.JrpcBootstrap;
import com.jackpang.loadBalancer.AbstractLoadBalancer;
import com.jackpang.loadBalancer.Selector;
import com.jackpang.transport.message.JrpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description: RoundRobinLoadBalancer
 * date: 11/21/23 7:48 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistentHashSelector(serviceList, 128);
    }

    /**
     * ConsistentHashSelector
     */
    private static class ConsistentHashSelector implements Selector {
        // hash circle for consistent hash
        private SortedMap<Integer, InetSocketAddress> circle = new TreeMap<>();
        // virtual node number
        private int virtualNodes;
        private AtomicInteger index;

        public ConsistentHashSelector(List<InetSocketAddress> serviceList, int virtualNodes) {
            this.virtualNodes = virtualNodes;
            for (InetSocketAddress inetSocketAddress : serviceList) {
                addNodeToCircle(inetSocketAddress);
            }
        }

        @Override
        public InetSocketAddress getNext() {
            // 1. get the content of the request
            JrpcRequest jrpcRequest = JrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            // 2. get the service by request feature
            String requestId = Long.toString(jrpcRequest.getRequestId());
            // 3. hash the request feature
            int hash = hash(requestId);
            // 4. get the service address by hash
            if (!circle.containsKey(hash)) {
                // 5. if the service address is null, get the first one
                SortedMap<Integer, InetSocketAddress> tailMap = circle.tailMap(hash);
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();

            }
            return circle.get(hash);
        }


        /**
         * add node to hash circle
         *
         * @param inetSocketAddress node address
         */
        private void addNodeToCircle(InetSocketAddress inetSocketAddress) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                circle.put(hash, inetSocketAddress);
                if (log.isDebugEnabled()) {
                    log.debug("Add node to hash circle: [{}], hash[{}]", inetSocketAddress, hash);
                }
            }
        }

        private void removeNodeFromCircle(InetSocketAddress inetSocketAddress) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(inetSocketAddress.toString() + "-" + i);
                circle.remove(hash, inetSocketAddress);
            }
        }

        private int hash(String s) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            byte[] digest = md5.digest(s.getBytes());
            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = res << 8 | digest[i];
            }
            return res;
        }

    }
}
