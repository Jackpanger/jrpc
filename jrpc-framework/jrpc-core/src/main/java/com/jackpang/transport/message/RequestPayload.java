package com.jackpang.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * description: describe the interface method in the request
 * date: 11/4/23 11:26 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPayload implements Serializable {
    // 1. interface name -- com.jackpang.HelloJrpc
    private String interfaceName;
    // 2. method name -- sayHi
    private String methodName;
    // 3. parameter list, include parameter type and parameters-- [hi there!]
    // parameter type ensures overloaded methods can be distinguished
    // specific parameter is to execute the method
    private Class<?>[] parametersType; // {java.lang.String}
    private Object[] parametersValue; // {hi there!}
    // 4 return value
    private Class<?> returnType; // java.lang.String
}
