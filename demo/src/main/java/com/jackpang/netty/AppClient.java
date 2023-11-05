package com.jackpang.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * description: Client
 * date: 11/3/23 10:49 AM
 * author: jinhao_pang
 * version: 1.0
 */
public class AppClient {
    public void run() {
        NioEventLoopGroup group = new NioEventLoopGroup();

        // launch client
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap = bootstrap.group(group)
                    .remoteAddress(new InetSocketAddress(8080))
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ClientChannelHandler());
                        }
                    });
            // connect to server
            ChannelFuture channelFuture = null;

            channelFuture = bootstrap.connect().sync();
            // acquire channel send data to server
            channelFuture.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty".getBytes(StandardCharsets.UTF_8)));
            // wait for server to close
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new AppClient().run();
    }
}
