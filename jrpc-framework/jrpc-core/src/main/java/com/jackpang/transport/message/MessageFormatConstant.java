package com.jackpang.transport.message;

/**
 * description: MessageFormatConstant
 * date: 11/4/23 11:51 PM
 * author: jinhao_pang
 * version: 1.0
 */
public class MessageFormatConstant {
    public static final byte[] MAGIC_NUMBER = "jrpc".getBytes();
    public static final byte VERSION = 1;
    public static final short HEADER_LENGTH = (short) (MAGIC_NUMBER.length+1+2+4+1+1+1+8);
    public static final short FULL_LENGTH = 4;
}
