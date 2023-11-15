package com.jackpang.serialize.impl;

import com.alibaba.fastjson2.JSON;
import com.jackpang.exceptions.SerializeException;
import com.jackpang.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * description: JsonSerializer
 * date: 11/5/23 5:21 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return new byte[0];
        }
        byte[] jsonBytes = JSON.toJSONBytes(object);
        if (log.isDebugEnabled()) {
            log.debug("Serialization object[{}] success", object);
        }
        return jsonBytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        // deserialization
        T object = JSON.parseObject(bytes, clazz);
        if (log.isDebugEnabled()) {
            log.debug("Class[{}] deserialization success", clazz);
        }
        return object;
    }
}
