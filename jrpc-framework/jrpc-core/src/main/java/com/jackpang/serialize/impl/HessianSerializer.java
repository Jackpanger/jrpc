package com.jackpang.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
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

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
            hessian2Output.writeObject(object);
            hessian2Output.flush();
            if (log.isDebugEnabled()) {
                log.debug("Serialization by hessian object[{}] success", object);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Serialization by hessian object[{}] error:{}", object, e.getMessage());
            throw new SerializeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        // deserialization
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            Hessian2Input hessian2Input = new Hessian2Input(bis);
            Object object = hessian2Input.readObject();
            if (log.isDebugEnabled()) {
                log.debug("Class[{}] deserialization by hessian success", clazz);
            }
            return (T) object;
        } catch (IOException e) {
            log.error("Class[{}] deserialization by hessian error:{}", clazz, e.getMessage());
            throw new SerializeException(e);
        }
    }
}
