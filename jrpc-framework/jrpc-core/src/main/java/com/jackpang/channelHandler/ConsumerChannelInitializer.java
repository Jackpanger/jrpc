package com.jackpang.channelHandler;

import com.jackpang.channelHandler.handler.JrpcMessageEncoder;
import com.jackpang.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * description: ConsumerChannelInitializer
 * date: 11/4/23 11:01 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
                // message encoder
                .addLast(new JrpcMessageEncoder())
                .addLast(new MySimpleChannelInboundHandler());
    }
}
