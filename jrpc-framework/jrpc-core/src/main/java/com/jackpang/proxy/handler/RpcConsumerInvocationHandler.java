package com.jackpang.proxy.handler;


import com.jackpang.JrpcBootstrap;
import com.jackpang.NettyBootstrapInitializer;
import com.jackpang.annotation.TryTimes;
import com.jackpang.compress.CompressorFactory;
import com.jackpang.discovery.Registry;
import com.jackpang.enumeration.RequestType;
import com.jackpang.exceptions.DiscoveryException;
import com.jackpang.exceptions.NetworkException;
import com.jackpang.protection.CircuitBreaker;
import com.jackpang.serialize.SerializerFactory;
import com.jackpang.transport.message.JrpcRequest;
import com.jackpang.transport.message.RequestPayload;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * description: encapsulate the proxy logic of the client side
 * 1. discover the service
 * 2. establish a connection with the service
 * 3. send a request to the service
 * 4. get the result from the service
 * date: 11/4/23 10:41 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {
    // registry center and interface
    private Registry registry;
    private Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


        // check if the method needs retry
        TryTimes annotation = method.getAnnotation(TryTimes.class);
        int tryTimes = 0;
        int intervalTime = 0;
        if (annotation != null) {
            tryTimes = annotation.tryTimes();
            intervalTime = annotation.intervalTime();
        }

        while (true) {
            // need retry when:
            // 1. error 2. code is wrong

            /*
             * ===================make a request packet===================
             */
            RequestPayload requestPayload = RequestPayload.builder()
                    .interfaceName(interfaceRef.getName())
                    .methodName(method.getName())
                    .parametersType(method.getParameterTypes())
                    .parametersValue(args)
                    .returnType(method.getReturnType())
                    .build();

            JrpcRequest jrpcRequest = JrpcRequest.builder()
                    .requestId(JrpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId())
                    .compressType(CompressorFactory.getCompressor(JrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCode())
                    .requestType(RequestType.REQUEST.getId())
                    .serializeType(SerializerFactory.getSerializer(JrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getCode())
                    .timeStamp(System.currentTimeMillis())
                    .requestPayload(requestPayload)
                    .build();

            JrpcBootstrap.REQUEST_THREAD_LOCAL.set(jrpcRequest);

            // 1. discover the service
            InetSocketAddress address = JrpcBootstrap.getInstance().getConfiguration().getLoadBalancer().selectServerAddress(interfaceRef.getName());
            if (log.isDebugEnabled()) {
                log.debug("Get the service {} address:{}", interfaceRef.getName(), address);
            }

            // Use netty to send data to server and get the result
            // 2. get a channel from the cache
            Channel channel = getAvailableChannel(address);
            if (log.isDebugEnabled()) {
                log.info("get a channel with address:{}, sending data..", address);
            }

            // Get the circuit breaker from the address
            Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = JrpcBootstrap.getInstance().getConfiguration().getEveryIpCircuitBreaker();
            CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(address);
            if (circuitBreaker == null) {
                circuitBreaker = new CircuitBreaker(10, 0.5f);
                everyIpCircuitBreaker.put(address, circuitBreaker);
            }
            try {
                if (circuitBreaker.isBreak()) {
                    // reset the circuit breaker periodically
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            everyIpCircuitBreaker.get(address).reset();
                        }
                    }, 3000);
                    throw new RuntimeException("The service is not available, circuit breaker is open.");
                }


                /*
                 * ====================sync====================
                 */
//            ChannelFuture channelFuture = channel.writeAndFlush(new Object()).await();
//            if (channelFuture.isDone()){
//                Object object = channelFuture.getNow();
//            } else if (!channelFuture.isSuccess()) {
//                Throwable cause = channelFuture.cause();
//                throw new RuntimeException(cause);
//            }
                /*
                 * ====================async====================
                 */
                // 4. write the package to the channel
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                JrpcBootstrap.PENDING_REQUEST.put(jrpcRequest.getRequestId(), completableFuture);
                // write request into pipeline to do outbound operation
                // jrpcRequest -> binary packet
                channel.writeAndFlush(jrpcRequest).addListener((ChannelFutureListener) promise -> {
//                if (promise.isSuccess()) {
//                    completableFuture.complete(promise.getNow());}
                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });

                // clear the thread local
                JrpcBootstrap.REQUEST_THREAD_LOCAL.remove();


                // block until the completableFuture is handled
                Object result = completableFuture.get(10, TimeUnit.SECONDS);
                // record the success request
                circuitBreaker.recordRequest();
                return result;
            } catch (Exception e) {
                tryTimes--;
                // record the error request
                circuitBreaker.recordErrorRequest();
                Thread.sleep(intervalTime);
                if (tryTimes < 0) {
                    log.error("Failed to send a request, tried {} times, error message: {}", tryTimes, e.getMessage());
                    break;
                }
                log.error("Failed to send a request, retrying..", e);
            }
        }
        throw new RuntimeException("Failed to send a request by method " + method.getName());
    }

    /**
     * get a channel from the cache by address
     *
     * @param address service address
     * @return channel
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        // 1. try to get a channel from the cache
        Channel channel = JrpcBootstrap.CHANNEL_CACHE.get(address);
        if (channel == null) {
            // channel = NettyBootstrapInitializer.getBootstrap().connect(address).await().channel();
            // block until the channel is available
            CompletableFuture<Channel> channelFuture = new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().
                    connect(address).addListener((ChannelFutureListener) promise -> {
                        if (promise.isDone()) {
                            // async
                            if (log.isDebugEnabled())
                                log.debug("Channel is available.");
                            channelFuture.complete(promise.channel());
                        } else if (!promise.isSuccess()) {
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    });
            // block until the channel is available
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.info("Failed to get a channel.", e);
                throw new DiscoveryException(e);
            }
            // put the channel into the cache
            JrpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            log.error("Failed to get a channel, address: {}.", address);
            throw new NetworkException("Failed to get a channel.");
        }
        return channel;
    }
}
