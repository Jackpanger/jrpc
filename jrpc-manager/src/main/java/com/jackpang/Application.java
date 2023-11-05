package com.jackpang;

import com.jackpang.utils.zookeeper.ZookeeperNode;
import com.jackpang.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.List;

import static com.jackpang.Constant.DEFAULT_ZK_CONNECT;
import static com.jackpang.Constant.TIME_OUT;

/**
 * description: registration center management
 * date: 11/4/23 2:38 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class Application {
    private static String basePath;

    public static void main(String[] args) throws InterruptedException {
        // Create basic directory
        // Create a ZooKeeper client
        ZooKeeper zooKeeper = ZookeeperUtils.createZookeeper(DEFAULT_ZK_CONNECT, TIME_OUT);
        String basePath = "/jrpc-metadata";
        String providerPath = basePath + "/providers";
        String consumerPath = basePath + "/consumers";

        ZookeeperNode zookeeperNode1 = new ZookeeperNode(basePath, null);
        ZookeeperNode zookeeperNode2 = new ZookeeperNode(providerPath, null);
        ZookeeperNode zookeeperNode3 = new ZookeeperNode(consumerPath, null);
        List.of(zookeeperNode1, zookeeperNode2, zookeeperNode3).forEach(node -> {
            ZookeeperUtils.createNode(zooKeeper, node, null, CreateMode.PERSISTENT);
        });
        zooKeeper.close();

    }
}
