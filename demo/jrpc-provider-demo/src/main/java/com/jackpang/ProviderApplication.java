package com.jackpang;

import com.jackpang.discovery.RegistryConfig;
import com.jackpang.impl.HelloJrpcImpl;

/**
 * description: Application
 * date: 11/3/23 10:26 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class ProviderApplication {
    public static void main(String[] args) {
        // Provider needs to register service and start service
        // 1. Encapsulate the service that to be published.
        ServiceConfig service = new ServiceConfig();
        service.setInterface(HelloJrpc.class);
        service.setRef(new HelloJrpcImpl());
        // 2. Define registration center

        // 2. Start the service provider by bootstrapping.
        //  (1) register the service to the registration center,
        //      others like serialization protocol, compression, etc.
        //  (2) publish the service

        JrpcBootstrap.getInstance()
                .application("first-jrpc-provider")
                // configure registration centerF
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                // publish service
//                .publish(service)
                .scan("com.jackpang")
                // start service
                .start();
    }
}
