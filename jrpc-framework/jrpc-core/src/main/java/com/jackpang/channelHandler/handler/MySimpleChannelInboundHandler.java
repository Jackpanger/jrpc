package com.jackpang.channelHandler.handler;

import com.jackpang.JrpcBootstrap;
import com.jackpang.enumeration.RespCode;
import com.jackpang.exceptions.ResponseException;
import com.jackpang.protection.CircuitBreaker;
import com.jackpang.transport.message.JrpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Map;
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

        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = JrpcBootstrap.getInstance().getConfiguration().getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);
        CompletableFuture<Object> completableFuture = JrpcBootstrap.PENDING_REQUEST.get(jrpcResponse.getRequestId());

        byte code = jrpcResponse.getCode();
        if (code == RespCode.FAIL.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("Request[{}] fail, resp code: [{}]", jrpcResponse.getRequestId(), code);
            throw new ResponseException(code, RespCode.FAIL.getDesc());
        } else if (code == RespCode.RATE_LIMIT.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("Request[{}] rate limit, resp code: [{}]", jrpcResponse.getRequestId(), code);
            throw new ResponseException(code, RespCode.RATE_LIMIT.getDesc());
        } else if (code == RespCode.RESOURCE_NOT_FOUND.getCode()) {
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("Request[{}] not found, resp code: [{}]", jrpcResponse.getRequestId(), code);
            throw new ResponseException(code, RespCode.RESOURCE_NOT_FOUND.getDesc());
        } else if (code == RespCode.SUCCESS.getCode()) {
            // result from service by provider
            Object returnValue = jrpcResponse.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()) {
                log.debug("Request[{}] finish completableFuture, resp: {}", jrpcResponse.getRequestId(), returnValue);
            }
        }else if (code == RespCode.SUCCESS_HEART_BEAT.getCode()) {
            // heartbeat response
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("Request[{}] finish completableFuture, resp: {}", jrpcResponse.getRequestId(), code);
            }
        }
    }
}
