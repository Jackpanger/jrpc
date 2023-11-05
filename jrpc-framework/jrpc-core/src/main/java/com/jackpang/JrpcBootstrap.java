package com.jackpang;

import com.jackpang.channelHandler.handler.JrpcMessageDecoder;
import com.jackpang.channelHandler.handler.MethodCallHandler;
import com.jackpang.discovery.Registry;
import com.jackpang.discovery.RegistryConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: JrpcBootStrap
 * date: 11/3/23 10:47 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class JrpcBootstrap {


    // JrpcBootstrap is a singleton class, so each application has only one instance.
    private static final JrpcBootstrap jrpcBootstrap = new JrpcBootstrap();

    // declare basic configuration
    private String appName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private int port = 8088;
    private Registry registry;

    // connection cache
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    // record the service published by the provider
    public static final Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);

    // global pending completable future
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    private JrpcBootstrap() {
        // Initialization when constructing the bootstrap.
    }

    public static JrpcBootstrap getInstance() {
        return jrpcBootstrap;
    }

    /**
     * Configure the application name.
     *
     * @param appName application name
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap application(String appName) {
        this.appName = appName;
        return this;
    }

    /**
     * Configure the registration center.
     *
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * Configure the protocol.
     *
     * @param protocolConfig protocol configuration
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()) {
            log.debug("Current protocolConfig:{}", protocolConfig.toString() + "protocol");
        }
        return this;
    }

    /*
     * -------------------API related to service provider-----------------------------
     */

    /**
     * Publish the service.
     *
     * @param service service to be published
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap publish(ServiceConfig<?> service) {
        // service node
        registry.register(service);
        SERVERS_LIST.put(service.getInterface().getName(), service);
        return this;
    }

    /**
     * Publish the services in batches.
     *
     * @param service service list to be published
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap publish(List<ServiceConfig<?>> service) {
        service.forEach(this::publish);
        return this;
    }

    /**
     * Start the service provider.
     */
    public void start() {
        EventLoopGroup boss = new NioEventLoopGroup(2);
        EventLoopGroup worker = new NioEventLoopGroup(10);
        try {
            // create server bootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // core logic
                            socketChannel.pipeline().addLast(new LoggingHandler())
                                    .addLast(new JrpcMessageDecoder())
                                    .addLast(new MethodCallHandler());
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    /*
     * -------------------API related to service consumer-----------------------------
     */

    public JrpcBootstrap reference(ReferenceConfig<?> reference) {
        reference.setRegistry(registry);
        return this;
    }

}
