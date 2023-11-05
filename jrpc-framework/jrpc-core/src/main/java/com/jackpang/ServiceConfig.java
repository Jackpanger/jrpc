package com.jackpang;

/**
 * description: ServiceConfig
 * date: 11/3/23 11:39 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class ServiceConfig<T>{
    private Class<T> interfaceProvider;
    private Object ref;

    public Class<T> getInterface() {
        return interfaceProvider;
    }

    public void setInterface(Class<T> interfaceProvider) {
        this.interfaceProvider = interfaceProvider;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    public Object getRef() {
        return ref;
    }
}
