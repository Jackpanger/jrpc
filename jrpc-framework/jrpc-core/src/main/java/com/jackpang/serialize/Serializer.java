package com.jackpang.serialize;

/**
 * description: Serializer
 * date: 11/5/23 5:05 PM
 * author: jinhao_pang
 * version: 1.0
 */
public interface Serializer {
    /**
     * serialize
     * @param object class instance
     * @return byte[]
     */
    byte[] serialize(Object object);

    /**
     * deserialize
     * @param bytes bytes
     * @param clazz class instance
     * @param <T> T
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
