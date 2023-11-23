package com.jackpang.discovery.impl;

import com.jackpang.Constant;
import com.jackpang.ServiceConfig;
import com.jackpang.discovery.AbstractRegistry;
import com.jackpang.utils.NetUtils;
import com.jackpang.utils.zookeeper.ZookeeperNode;
import com.jackpang.utils.zookeeper.ZookeeperUtils;
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
public class NacosRegistry extends AbstractRegistry {
    private ZooKeeper zooKeeper;

    public NacosRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }

    public NacosRegistry(String connectString, int timeout) {
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectString, timeout);
    }

    @Override
    public void register(ServiceConfig<?> service) {
        String parentNode = Constant.BASE_PROVIDERS_PATH + "/" + service.getInterface().getName();

        ZookeeperUtils.createNode(zooKeeper, new ZookeeperNode(parentNode, null), null, CreateMode.PERSISTENT);
        String node = parentNode + "/" + NetUtils.getIp() + ":" + 8088;
        ZookeeperUtils.createNode(zooKeeper, new ZookeeperNode(node, null), null, CreateMode.EPHEMERAL);


        if (log.isDebugEnabled()) {
            log.debug("Service is registered:{}", service.toString());
        }

    }

    @Override
    public List<InetSocketAddress> lookup(String serviceName) {
        return null;
    }
}
