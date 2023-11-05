package com.jackpang;

import com.jackpang.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * description: Application
 * date: 11/3/23 10:33 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class ConsumerApplication {
    public static void main(String[] args) {
        // use referenceConfig to encapsulate the service
        // there must be a template method in the reference.
        ReferenceConfig<HelloJrpc> reference = new ReferenceConfig<>();
        reference.setInterface(HelloJrpc.class);

        // proxy:
        // 1. access to the zookeeper,
        // 2. fetch the service list,
        // 3. choose a service then connect to it
        // 4. send a request to the service with some information
        // (interface name, parameter list, method name.) to get a response.

        JrpcBootstrap.getInstance()
                .application("first-jrpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(reference);

        // get a proxy object
        HelloJrpc helloJrpc = reference.get();
        String sayHi = helloJrpc.sayHi("hi there!");
        log.info("result:{}", sayHi);

    }
}
