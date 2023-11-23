package com.jackpang;

import com.jackpang.discovery.Registry;
import com.jackpang.discovery.RegistryConfig;
import com.jackpang.loadBalancer.LoadBalancer;
import com.jackpang.loadBalancer.impl.RoundRobinLoadBalancer;
import com.jackpang.transport.message.JrpcRequest;
import io.netty.channel.Channel;
import lombok.Data;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global configuration, code configuration -> xml configuration -> default configuration
 * date: 11/23/23 3:49 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Data
public class Configuration {

    // configuration for port
    private final int port = 8089;

    // configuration for application name
    private String appName = "default";
    // configuration for registry
    private RegistryConfig registryConfig;
    // configuration for serialize protocol
    private ProtocolConfig protocolConfig;
    // configuration for serialize and compress
    private String serializeType = "jdk";
    private String compressType = "gzip";

    // configuration for load balancer
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    // configuration for id generator
    private IdGenerator idGenerator = new IdGenerator(1L, 1L);


    // read xml configuration

    public Configuration() {
        // read xml configuration
    }


    // configure
}
