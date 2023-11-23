package com.jackpang.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: ObjectWrapper
 * date: 11/23/23 9:27 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectWrapper<T> {
    private Byte code;
    private String name;
    private T impl;
}
