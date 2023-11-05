package com.jackpang.netty;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * description: WatcherTest
 * date: 11/3/23 5:06 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class CustomWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Event.EventType.None) {
            if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                System.out.println("connected");
            } else if (watchedEvent.getState() == Event.KeeperState.Disconnected) {
                System.out.println("disconnected");
            } else if (watchedEvent.getState() == Event.KeeperState.Expired) {
                System.out.println("expired");
            } else if (watchedEvent.getState() == Event.KeeperState.AuthFailed) {
                System.out.println("auth failed");
            }
        } else if (watchedEvent.getType() == Event.EventType.NodeCreated) {
            System.out.println(watchedEvent.getPath() + "node created");
        } else if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
            System.out.println(watchedEvent.getPath() + "node deleted");
        } else if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
            System.out.println(watchedEvent.getPath() + "node data changed");
        } else if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
            System.out.println(watchedEvent.getPath() + "node children changed");
        }
    }
}
