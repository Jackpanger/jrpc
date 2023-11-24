package com.jackpang;

import com.jackpang.annotation.JrpcService;
import com.jackpang.proxy.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * description: JrpcProxyBeanPostProcessor
 * date: 11/24/23 6:28 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Component
public class JrpcProxyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            JrpcService jrpcService = field.getAnnotation(JrpcService.class);
            if (jrpcService != null) {
                Class<?> type = field.getType();
                field.setAccessible(true);
                try {
                    field.set(bean, ProxyFactory.getProxy(type));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bean;
    }
}
