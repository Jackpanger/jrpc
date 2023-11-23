package com.jackpang.serialize;

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
    private final static Map<String, SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SerializerWrapper hessian = new SerializerWrapper((byte) 3, "hessian", new HessianSerializer());
        SERIALIZER_CACHE.put("jdk", jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE.put("hessian",hessian);
        SERIALIZER_CACHE_CODE.put((byte) 1, jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2, json);
        SERIALIZER_CACHE_CODE.put((byte) 3, hessian);

    }
    public static SerializerWrapper getSerializer(String serializeType) {

        SerializerWrapper serializerWrapper = SERIALIZER_CACHE.get(serializeType);
        if (serializerWrapper == null) {
            if (log.isDebugEnabled()){
                log.error("Serializer not found, use default jdk serializer");
            }
            return SERIALIZER_CACHE.get("jdk");
        }
        return serializerWrapper;
    }

    public static SerializerWrapper getSerializer(byte serializeCode) {
        SerializerWrapper serializerWrapper = SERIALIZER_CACHE_CODE.get(serializeCode);
        if (serializerWrapper == null) {
            if (log.isDebugEnabled()){
                log.error("Serializer not found, use default jdk serializer");
            }
            return SERIALIZER_CACHE_CODE.get((byte) 1);
        }
        return serializerWrapper;
    }
}
