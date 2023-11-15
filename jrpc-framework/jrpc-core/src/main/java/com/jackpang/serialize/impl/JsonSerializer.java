package com.jackpang.serialize.impl;

import com.jackpang.serialize.Serializer;

/**
 * description: JsonSerializer
 * date: 11/5/23 5:21 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        return new byte[0];
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        return null;
    }
}
