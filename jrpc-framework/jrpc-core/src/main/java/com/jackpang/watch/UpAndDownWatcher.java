package com.jackpang.watch;

import com.jackpang.JrpcBootstrap;
import com.jackpang.NettyBootstrapInitializer;
import com.jackpang.discovery.Registry;
import com.jackpang.loadBalancer.LoadBalancer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * description: UpAndDownWatcher
 * date: 11/22/23 4:34 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
        if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
            if (log.isDebugEnabled()) {
                log.debug("NodeChildrenChanged, node [{}] up or down, fetching latest nodes", watchedEvent.getPath());
            }
            String serviceName = getServiceName(watchedEvent.getPath());
            Registry registry = JrpcBootstrap.getInstance().getRegistry();
            List<InetSocketAddress> addresses = registry.lookup(serviceName);
            addresses.forEach(address -> {
                if (!JrpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel;
                    try {
                        channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    } catch (Exception e) {
                        try {
                            Thread.sleep(1000);
                            channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    JrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            });
            JrpcBootstrap.CHANNEL_CACHE.forEach((key, value) -> {
                if (!addresses.contains(key)) {
                    JrpcBootstrap.CHANNEL_CACHE.remove(key);
                }
            });

            // get loadBalancer and reload
            LoadBalancer loadBalancer = JrpcBootstrap.LOAD_BALANCER;
            loadBalancer.reloadBalance(serviceName, addresses);

        }
    }

    private String getServiceName(String path) {
        String[] split = path.split("/");
        return split[split.length - 1];
    }
}
