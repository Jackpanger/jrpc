package com.jackpang.channelHandler.handler;

import com.jackpang.JrpcBootstrap;
import com.jackpang.ServiceConfig;
import com.jackpang.core.ShutdownHolder;
import com.jackpang.enumeration.RequestType;
import com.jackpang.enumeration.RespCode;
import com.jackpang.protection.RateLimiter;
import com.jackpang.protection.TokenBucketRateLimiter;
import com.jackpang.transport.message.JrpcRequest;
import com.jackpang.transport.message.JrpcResponse;
import com.jackpang.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * description: MethodCallHandler
 * date: 11/5/23 10:13 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<JrpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, JrpcRequest jrpcRequest) throws Exception {

        // Encapsulate the return value into JrpcResponse
        JrpcResponse jrpcResponse = new JrpcResponse();
        jrpcResponse.setRequestId(jrpcRequest.getRequestId());
        jrpcResponse.setCompressType((jrpcRequest.getCompressType()));
        jrpcResponse.setSerializeType(jrpcRequest.getSerializeType());
        Channel channel = channelHandlerContext.channel();

        if (ShutdownHolder.BAFFLE.get()) {
            jrpcResponse.setCode(RespCode.CLOSING.getCode());
            channel.writeAndFlush(jrpcResponse);
            return;
        }

        ShutdownHolder.REQUEST_COUNTER.increment();

        // limit the number of requests per second
        SocketAddress socketAddress = channel.remoteAddress();
        RateLimiter rateLimiter = JrpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter().get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBucketRateLimiter(5, 5);
            JrpcBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter().put(socketAddress, rateLimiter);
        }

        if (!rateLimiter.allowRequest()) {
            jrpcResponse.setCode(RespCode.RATE_LIMIT.getCode());

        } else if (jrpcRequest.getRequestType() == RequestType.HEARTBEAT.getId()) {
            // check if the request is heartbeat
            jrpcResponse.setCode(RespCode.SUCCESS_HEART_BEAT.getCode());


        } else {
            /* -----------------concrete call method ----------------- */

            // 1. Get payload from JrpcRequest
            RequestPayload requestPayload = jrpcRequest.getRequestPayload();
            // 2. Call the corresponding method
            try {
                Object result = callTargetMethod(requestPayload);

                if (log.isDebugEnabled()) {
                    log.debug("Call method[{}] in service[{}] success", requestPayload.getMethodName(), requestPayload.getInterfaceName());
                }
                // 3. Encapsulate the return value into JrpcResponse
                jrpcResponse.setCode(RespCode.SUCCESS.getCode());
                jrpcResponse.setBody(result);
            } catch (Exception e) {
                log.error("Request id [{}] Call method[{}] in service[{}] error", jrpcRequest.getRequestId(), requestPayload.getMethodName(), requestPayload.getInterfaceName(), e);
                jrpcResponse.setCode(RespCode.FAIL.getCode());
            }
        }
        // 4. Write JrpcResponse to the channel
        channel.writeAndFlush(jrpcResponse);
        ShutdownHolder.REQUEST_COUNTER.decrement();
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        // Get the corresponding service object
        ServiceConfig serviceConfig = JrpcBootstrap.SERVERS_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();
        // Call the corresponding method by reflection
        // 1. get method object 2. invoke
        Class<?> aClass = refImpl.getClass();
        Object returnValue;
        try {
            Method method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("Call method[{}] in service[{}] error", methodName, interfaceName, e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
