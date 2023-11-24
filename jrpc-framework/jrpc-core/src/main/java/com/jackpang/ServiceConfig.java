package com.jackpang;

import lombok.Getter;

/**
 * description: ServiceConfig
 * date: 11/3/23 11:39 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class ServiceConfig {
    private Class<?> interfaceProvider;
    @Getter
    private Object ref;
    @Getter
    private String group;

    public Class<?> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<?> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public void setGroup(String group) {
        this.group = group;
    }

}
