package com.jackpang.serialize;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * description: serializeUtils
 * date: 11/5/23 5:03 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class SerializeUtils {
    public static byte[] serialize(Object object){
        if (object == null) {
            return new byte[0];
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Serialization error:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
