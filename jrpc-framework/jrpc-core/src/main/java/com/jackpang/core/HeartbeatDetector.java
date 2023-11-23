package com.jackpang.core;

import com.jackpang.JrpcBootstrap;
import com.jackpang.NettyBootstrapInitializer;
import com.jackpang.compress.CompressorFactory;
import com.jackpang.discovery.Registry;
import com.jackpang.enumeration.RequestType;
import com.jackpang.serialize.SerializerFactory;
import com.jackpang.transport.message.JrpcRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import static com.jackpang.JrpcBootstrap.getInstance;

/**
 * description: HeartbeatDetector
 * date: 11/22/23 12:14 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class HeartbeatDetector {
    public static void detect(String serviceName) {
        // fetch service list from registry center and establish connection
        Registry registry = JrpcBootstrap.getInstance().getConfiguration().getRegistryConfig().getRegistry();
        List<InetSocketAddress> addresses = registry.lookup(serviceName);
        // cache the connection
        for (InetSocketAddress address : addresses) {
            try {
                if (!JrpcBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap().
                            connect(address).sync().channel();
                    JrpcBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // send heartbeat to the server periodically
        Thread thread = new Thread(
                () -> new Timer().scheduleAtFixedRate(new MyTimerTask(), 0, 2000),
                "jrpc-heartbeatDetector-thread"
        );
        thread.setDaemon(true);
        thread.start();
    }

    private static class MyTimerTask extends TimerTask {


        @Override
        public void run() {
            JrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.clear();

            Map<InetSocketAddress, Channel> channelCache = JrpcBootstrap.CHANNEL_CACHE;
            channelCache.forEach((address, channel) -> {
                int tryTimes = 3;
                while (tryTimes > 0) {
                    long start = System.currentTimeMillis();
                    // construct a heartbeat request packet
                    JrpcRequest jrpcRequest = JrpcRequest.builder()
                            .requestId(getInstance().getConfiguration().getIdGenerator().getId())
                            .compressType(CompressorFactory.getCompressor(getInstance().getConfiguration().getCompressType()).getCode())
                            .requestType(RequestType.HEARTBEAT.getId())
                            .serializeType(SerializerFactory.getSerializer(getInstance().getConfiguration().getSerializeType()).getCode())
                            .timeStamp(start)
                            .build();
                    // write the package to the channel
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    JrpcBootstrap.PENDING_REQUEST.put(jrpcRequest.getRequestId(), completableFuture);
                    channel.writeAndFlush(jrpcRequest).addListener((ChannelFutureListener) future -> {
                        if (!future.isSuccess()) {
                            completableFuture.completeExceptionally(future.cause());
                        }
                    });
                    long endTime = 0;
                    try {
                        completableFuture.get(1, TimeUnit.SECONDS);
                        endTime = System.currentTimeMillis();
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        tryTimes--;
                        log.error("heartbeat error connecting [{}], leaving [{}] trying times", address, tryTimes);
                        // remove the channel from the cache list
                        if (tryTimes == 0){
                            JrpcBootstrap.CHANNEL_CACHE.remove(address);
                        }
                        // Wait for a while
                        try {
                            Thread.sleep(10L *(new Random().nextInt(5)));
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                        continue;
                    }
                    Long time = endTime - start;
                    // Use treemap to sort the time
                    JrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.put(time, channel);
                    log.debug("heartbeat time:[{}]ms with server [{}]", time, address);
                    break;
                }
            });
            log.info("=========================heartbeat response time=========================");
            JrpcBootstrap.ANSWER_TIME_CHANNEL_CACHE.forEach((time, channel) -> {
                if (log.isDebugEnabled())
                    log.info("heartbeat time:[{}]ms with channel id [{}]", time, channel.id());
            });
        }
    }
}
