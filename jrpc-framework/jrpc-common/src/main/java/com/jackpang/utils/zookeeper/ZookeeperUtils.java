package com.jackpang.utils.zookeeper;

import com.jackpang.exceptions.ZookeeperException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.jackpang.Constant.DEFAULT_ZK_CONNECT;
import static com.jackpang.Constant.TIME_OUT;

/**
 * description: ZookeeperUtil
 * date: 11/4/23 2:59 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class ZookeeperUtils {
    public static ZooKeeper createZookeeper() {
        return createZookeeper(DEFAULT_ZK_CONNECT, TIME_OUT);
    }

    public static ZooKeeper createZookeeper(String connectString, int timeout) {
        // Create basic directory
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            // Create a ZooKeeper client
            ZooKeeper zooKeeper = new ZooKeeper(connectString, timeout, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.debug("connected to zookeeper");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.error("Create base path failed", e);
            throw new ZookeeperException();
        }
    }

    /**
     * Create a node
     * @param zooKeeper zookeeper client
     * @param node node
     * @param watcher watcher
     * @param createMode CreateMode
     * @return Boolean
     */
    public static boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, CreateMode createMode) {
        try {
            if (zooKeeper.exists(node.getNodePath(), watcher) == null) {
                String result = zooKeeper.create(node.getNodePath(), node.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
                log.info("Create base path: {} {}}", node.getNodePath(), result);
                return true;
            }else {
                if (log.isDebugEnabled()) {
                    log.debug("Node already exists: {}", node.getNodePath());
                }
                return false;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("Create base path failed", e);
            throw new ZookeeperException();
        }
    }

    /**
     * Get the children of the node
     *
     * @param zooKeeper   zookeeper client
     * @param serviceNode service node
     * @return String[]
     */
    public static List<String> getChildren(ZooKeeper zooKeeper, String serviceNode, Watcher watcher) {
        try {
            return zooKeeper.getChildren(serviceNode, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("Get children[{}]  failed", serviceNode, e);
            throw new RuntimeException(e);
        }
    }
}
