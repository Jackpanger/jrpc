package com.jackpang.discovery;

import com.jackpang.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * description: Registry
 * date: 11/4/23 6:20 PM
 * author: jinhao_pang
 * version: 1.0
 */
public interface Registry {
    /**
     * Register the service to the registration center.
     *
     * @param serviceConfig service configuration
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * Lookup the service from the registration center.
     * @param name service name
     * @return service address
     */
    InetSocketAddress lookup(String serviceName);
}
