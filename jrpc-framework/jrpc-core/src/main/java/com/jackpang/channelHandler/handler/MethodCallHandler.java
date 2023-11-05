package com.jackpang.channelHandler.handler;

import com.jackpang.JrpcBootstrap;
import com.jackpang.ServiceConfig;
import com.jackpang.transport.message.JrpcRequest;
import com.jackpang.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        // 1. Get payload from JrpcRequest
        RequestPayload requestPayload = jrpcRequest.getRequestPayload();
        // 2. Call the corresponding method
        Object object = callTargetMethod(requestPayload);
        // 3. Encapsulate the return value into JrpcResponse

        // 4. Write JrpcResponse to the channel
        channelHandlerContext.channel().writeAndFlush(object);
    }

    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();

        // Get the corresponding service object
        ServiceConfig<?> serviceConfig = JrpcBootstrap.SERVERS_LIST.get(interfaceName);
        Object refImpl = serviceConfig.getRef();
        // Call the corresponding method by reflection
        // 1. get method object 2. invoke
        Class<?> aClass = refImpl.getClass();
        Object returnValue;
        try {
            Method method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("call method[{}] in service[{}] error", methodName, interfaceName, e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}