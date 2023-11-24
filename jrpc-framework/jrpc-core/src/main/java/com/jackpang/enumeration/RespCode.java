package com.jackpang.enumeration;

/**
 * description: RespCode
 * success code: 20(success) 21(heartBeat)
 * fail code: 50(not found， service not found) 44(not found, client not found)
 * payload code: 31 (too many request, limit the number of requests per second)
 */
public enum RespCode {
    SUCCESS((byte) 20, "success"),
    SUCCESS_HEART_BEAT((byte) 21, "heartBeat success"),
    RATE_LIMIT((byte) 31, "too many request, limit the number of requests per second"),
    FAIL((byte) 50, "method call error, service not found"),
    RESOURCE_NOT_FOUND((byte) 44, "not found, client not found");

    private byte code;
    private String desc;

    RespCode(byte code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public byte getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
