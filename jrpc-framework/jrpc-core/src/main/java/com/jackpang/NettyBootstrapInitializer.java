package com.jackpang;

import com.jackpang.channelHandler.ConsumerChannelInitializer;
import com.jackpang.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * description: provide bootstrap singleton
 * date: 11/4/23 8:25 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class NettyBootstrapInitializer {
    @Getter
    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ConsumerChannelInitializer());
    }

    private NettyBootstrapInitializer() {
    }

}
