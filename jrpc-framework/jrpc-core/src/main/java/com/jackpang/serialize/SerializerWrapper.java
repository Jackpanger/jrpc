package com.jackpang.serialize;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: SerializerWrapper
 * date: 11/5/23 5:24 PM
 * author: jinhao_pang
 * version: 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SerializerWrapper {
    private byte code;
    private String type;
    private Serializer serializer;
}
