package com.jackpang.serialize.impl;

import com.jackpang.exceptions.SerializeException;
import com.jackpang.serialize.Serializer;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * description: JdkSerializer
 * date: 11/5/23 5:08 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        if (object == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            outputStream.writeObject(object);
            if (log.isDebugEnabled()){
                log.debug("Serialization object[{}] success", object);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Serialization object[{}] error:{}", object, e.getMessage());
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        // deserialization
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            Object object =  ois.readObject();
            if (log.isDebugEnabled()){
                log.debug("Class[{}] deserialization success", clazz);
            }
            return (T) object;
        } catch (IOException | ClassNotFoundException e) {
            log.error("Class[{}] deserialization error:{}", clazz, e.getMessage());
            throw new SerializeException(e);
        }
    }
}
