package com.jackpang.config;

import com.jackpang.IdGenerator;
import com.jackpang.ProtocolConfig;
import com.jackpang.compress.Compressor;
import com.jackpang.compress.impl.GzipCompressor;
import com.jackpang.discovery.RegistryConfig;
import com.jackpang.loadBalancer.LoadBalancer;
import com.jackpang.loadBalancer.impl.RoundRobinLoadBalancer;
import com.jackpang.serialize.Serializer;
import com.jackpang.serialize.impl.JdkSerializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Global configuration, code configuration -> xml configuration -> default configuration
 * date: 11/23/23 3:49 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Data
@Slf4j
public class Configuration {

    // configuration for port
    private int port = 8089;

    // configuration for application name
    private String appName = "default";
    // configuration for registry
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");

    // configuration for serialize and compress
    private String serializeType = "jdk";
    private String compressType = "gzip";

    // configuration for load balancer
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    // configuration for id generator
    private IdGenerator idGenerator = new IdGenerator(1L, 1L);


    // read xml configuration

    public Configuration() {
        // default configuration

        // spi mechanism
        SpiResolver spiResolver = new SpiResolver();
        spiResolver.loadFromSpi(this);

        XmlResolver xmlResolver = new XmlResolver();
        // read xml configuration
        xmlResolver.loadFromXml(this);

        // jrpc-bootstrap
    }

    public static void main(String[] args) {
        Configuration configuration = new Configuration();
    }

}