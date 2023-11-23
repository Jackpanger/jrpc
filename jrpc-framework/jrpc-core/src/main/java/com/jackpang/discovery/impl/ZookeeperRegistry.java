package com.jackpang.discovery.impl;

import com.jackpang.Constant;
import com.jackpang.JrpcBootstrap;
import com.jackpang.ServiceConfig;
import com.jackpang.discovery.AbstractRegistry;
import com.jackpang.exceptions.DiscoveryException;
import com.jackpang.utils.NetUtils;
import com.jackpang.utils.zookeeper.ZookeeperNode;
import com.jackpang.utils.zookeeper.ZookeeperUtils;
import com.jackpang.watch.UpAndDownWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * description: ZookeeperRegistry
 * date: 11/4/23 6:23 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    private ZooKeeper zooKeeper;

    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public ZookeeperRegistry(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectString, timeout);
    }

    @Override
    public void register(ServiceConfig service) {
        String parentNode = Constant.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();

        ZookeeperUtils.createNode(zooKeeper, new ZookeeperNode(parentNode, null), null, CreateMode.PERSISTENT);
        String node = parentNode + "/" + NetUtils.getIp() + ":" + JrpcBootstrap.PORT;
        ZookeeperUtils.createNode(zooKeeper, new ZookeeperNode(node, null), null, CreateMode.EPHEMERAL);


        if (log.isDebugEnabled()) {
            log.debug("Service is registered:{}", service.toString());
        }

    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        // 1. get the service node
        String serviceNode = Constant.BASE_PROVIDERS_PATH + "/" + serviceName;
        // 2. get the children node
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNode, new UpAndDownWatcher());
        List<InetSocketAddress> list = children.stream().map(ipString -> {
            String[] ipPort = ipString.split(":");
            return new InetSocketAddress(ipPort[0], Integer.parseInt(ipPort[1]));
        }).toList();
        if (list.isEmpty()) {
            throw new DiscoveryException("No service available");
        }
        return list;
    }
}
