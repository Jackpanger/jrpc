package com.jackpang.loadBalancer;

import java.net.InetSocketAddress;
import java.util.List;

public interface LoadBalancer {
    /**
     * Select an available server by service name.
     *
     * @param serviceName service list
     * @return server address
     */
    InetSocketAddress selectServerAddress(String serviceName);

    /**
     * Reload the balance when node is up or down.
     *
     * @param serviceName service name
     * @param addresses
     */
    void reloadBalance(String serviceName, List<InetSocketAddress> addresses);
}
