package com.jackpang.enumeration;


public enum RespCode {
    SUCCESS((byte) 1, "success"), FAIL((byte) 2, "fail");
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
