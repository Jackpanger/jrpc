package com.jackpang.discovery;

import com.jackpang.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

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
    void register(ServiceConfig serviceConfig);

    /**
     * Lookup the list of services from the registration center.
     * @param serviceName service name
     * @return service address
     */
    List<InetSocketAddress> lookup(String serviceName);
}
