package com.jackpang.serialize;

import com.jackpang.serialize.impl.JdkSerializer;
import com.jackpang.serialize.impl.JsonSerializer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: SerializerFactory
 * date: 11/5/23 5:20 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class SerializerFactory {
    private final static Map<String, SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap<>();
    private final static Map<Byte, SerializerWrapper> SERIALIZER_CACHE_CODE = new ConcurrentHashMap<>();

    static {
        SerializerWrapper jdk = new SerializerWrapper((byte) 1, "jdk", new JdkSerializer());
        SerializerWrapper json = new SerializerWrapper((byte) 2, "json", new JsonSerializer());
        SERIALIZER_CACHE.put("jdk", jdk);
        SERIALIZER_CACHE.put("json",json);
        SERIALIZER_CACHE_CODE.put((byte) 1, jdk);
        SERIALIZER_CACHE_CODE.put((byte) 2, json);

    }
    public static SerializerWrapper getSerializer(String serializeType) {
       return SERIALIZER_CACHE.get(serializeType);
    }

    public static SerializerWrapper getSerializer(byte serializeCode) {
        return SERIALIZER_CACHE_CODE.get(serializeCode);
    }
}
