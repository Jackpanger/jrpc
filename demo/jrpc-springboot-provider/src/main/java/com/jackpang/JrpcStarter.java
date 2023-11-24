package com.jackpang;

import com.jackpang.discovery.RegistryConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * description: JrpcStarter
 * date: 11/24/23 6:14 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Component
@Slf4j
public class JrpcStarter implements CommandLineRunner
{
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        log.info("start jrpc...");
        JrpcBootstrap.getInstance()
                .application("first-jrpc-provider")
                // configure registration centerF
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .serialize("jdk")
                // publish service
//                .publish(service)
                .scan("com.jackpang.impl")
                // start service
                .start();
    }
}
