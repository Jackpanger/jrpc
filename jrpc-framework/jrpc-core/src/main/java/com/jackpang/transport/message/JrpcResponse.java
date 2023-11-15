package com.jackpang.transport.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: service provider response
 * date: 11/4/23 11:23 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JrpcResponse {
    // request id
    private long requestId;
    // serialization type
    private byte compressType;
    // serialize type
    private byte serializeType;
    // 1 success, 2 fail
    private byte code;
    // specific message body
    private Object body;

}
