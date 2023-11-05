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
    // bytes of header length
    public static final int HEADER_FIELD_LENGTH = 2;
    public static final int MAX_FRAME_LENGTH = 1024 * 1024;
    public static final int VERSION_LENGTH = 1;
    // bytes of full length
    public static final int FULL_FIELD_LENGTH = 4;
}
