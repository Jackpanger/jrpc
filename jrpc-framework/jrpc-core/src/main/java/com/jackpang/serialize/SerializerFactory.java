package com.jackpang.serialize;

import com.jackpang.config.ObjectWrapper;
import com.jackpang.serialize.impl.HessianSerializer;
import com.jackpang.serialize.impl.JdkSerializer;
import com.jackpang.serialize.impl.JsonSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: SerializerFactory
 * date: 11/5/23 5:20 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class SerializerFactory {
    private final static Map<String, ObjectWrapper<Serializer>> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, ObjectWrapper<Serializer>> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        ObjectWrapper<Serializer> jdk = new ObjectWrapper<>((byte) 1, "jdk", new JdkSerializer());
        ObjectWrapper<Serializer> json = new ObjectWrapper<>((byte) 2, "json", new JsonSerializer());
        ObjectWrapper<Serializer> hessian = new ObjectWrapper<>((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);
        SERIALIZER_CACHE_CODE.put((byte) 1, jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2, json);
        SERIALIZER_CACHE_CODE.put((byte) 3, hessian);

    }
    public static ObjectWrapper<Serializer> getSerializer(String serializeType) {

        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper == null) {
            if (log.isDebugEnabled()){
                log.error("Serializer not found, use default jdk serializer");
            }
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

    public static ObjectWrapper<Serializer> getSerializer(byte serializeCode) {
        ObjectWrapper<Serializer> serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if (serializerWrapper == null) {
            if (log.isDebugEnabled()){
                log.error("Serializer not found, use default jdk serializer");
            }
            return SERIALIZER_CACHE_CODE.get((byte) 1);
        }
        return serializerWrapper;
    }

    /**
     * add serializer to cache
     * @param serializerObjectWrapper
     */
    public static void addSerializer(ObjectWrapper<Serializer> serializerObjectWrapper) {
        SERIALIZER_CACHE.put(serializerObjectWrapper.getName(), serializerObjectWrapper);
        SERIALIZER_CACHE_CODE.put(serializerObjectWrapper.getCode(), serializerObjectWrapper);
    }
}
