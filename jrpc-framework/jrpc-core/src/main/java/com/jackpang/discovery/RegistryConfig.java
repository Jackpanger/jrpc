package com.jackpang.discovery;

import com.jackpang.Constant;
import com.jackpang.discovery.Registry;
import com.jackpang.discovery.impl.NacosRegistry;
import com.jackpang.discovery.impl.ZookeeperRegistry;
import com.jackpang.exceptions.DiscoveryException;

/**
 * description: RegistryConfig
 * date: 11/3/23 11:08 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class RegistryConfig {
    private final String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    /**
     * simple factory
     * @return Registry instance
     */
    public Registry getRegistry() {
        String registryType = getRegistryType(connectString, true).toLowerCase().trim();
        if ("zookeeper".equals(registryType)) {
            String host = getRegistryType(connectString, false);
            return new ZookeeperRegistry(host, Constant.TIME_OUT);
        } else if ("nacos".equals(registryType)) {
            String host = getRegistryType(connectString, false);
            return new NacosRegistry(host, Constant.TIME_OUT);
        }
        throw new DiscoveryException("Unsupported registry type");
    }

    private String getRegistryType(String connectString, boolean ifType) {
        String[] typeAndHost = connectString.split("://");
        if (typeAndHost.length != 2) {
            throw new IllegalArgumentException("Invalid registry address:" + connectString);
        }
        if (ifType) {
            return typeAndHost[0];
        } else {
            return typeAndHost[1];
        }
    }
}
