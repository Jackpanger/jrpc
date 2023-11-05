package com.jackpang.channelHandler.handler;

import com.jackpang.JrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * description: MySimpleChannelInboundHandler
 * date: 11/4/23 11:00 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        // result from server
        String result = byteBuf.toString(Charset.defaultCharset());
        CompletableFuture<Object> completableFuture = JrpcBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
