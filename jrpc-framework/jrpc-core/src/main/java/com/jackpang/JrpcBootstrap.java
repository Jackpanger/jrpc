package com.jackpang;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.logging.Handler;

/**
 * description: JrpcBootStrap
 * date: 11/3/23 10:47 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class JrpcBootstrap {


    // JrpcBootstrap is a singleton class, so each application has only one instance.
    private static JrpcBootstrap jrpcBootstrap = new JrpcBootstrap();

    private JrpcBootstrap() {
        // Initialization when constructing the bootstrap.
    }

    public static JrpcBootstrap getInstance() {
        return jrpcBootstrap;
    }

    /**
     * Configure the application name.
     *
     * @param appName application name
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap application(String appName) {
        return this;
    }

    /**
     * Configure the registration center.
     *
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * Configure the protocol.
     *
     * @param protocolConfig protocol configuration
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        if (log.isDebugEnabled()){
            log.debug("Current protocolConfig:{}", protocolConfig.toString()+"protocol");
        }
        return this;
    }

    /*
     * -------------------API related to service provider-----------------------------
     */

    /**
     * Publish the service.
     *
     * @param service service to be published
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap publish(ServiceConfig<?> service) {
        if (log.isDebugEnabled()){
            log.debug("Service is published:{}", service.toString());
        }
        return this;
    }

    /**
     * Publish the services in batches.
     *
     * @param service service list to be published
     * @return this JrpcBootstrap instance
     */
    public JrpcBootstrap publish(List<ServiceConfig<?>> service) {
        return this;
    }

    /**
     * Start the service provider.
     */
    public void start() {
    }


    /*
     * -------------------API related to service consumer-----------------------------
     */

    public JrpcBootstrap reference(ReferenceConfig<?> reference) {
        return this;
    }

}
