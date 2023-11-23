package com.jackpang.channelHandler.handler;

import com.jackpang.JrpcBootstrap;
import com.jackpang.transport.message.JrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * description: MySimpleChannelInboundHandler
 * date: 11/4/23 11:00 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<JrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JrpcResponse jrpcResponse) throws Exception {

        // result from service by provider
        Object returnValue = jrpcResponse.getBody();
        CompletableFuture<Object> completableFuture = JrpcBootstrap.PENDING_REQUEST.get(jrpcResponse.getRequestId());
        completableFuture.complete(returnValue);
        if (log.isDebugEnabled()){
            log.debug("Request[{}] finish completableFuture, resp: {}",jrpcResponse.getRequestId(), returnValue);
        }
    }
}
