package com.jackpang.transport.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description: service consumer request
 * date: 11/4/23 11:23 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JrpcRequest {
    // request id
    private long requestId;
    // request type
    private byte requestType;
    // serialization type
    private byte compressType;
    // serialize type
    private byte serializeType;
    // specific message body
    private RequestPayload requestPayload;

}
