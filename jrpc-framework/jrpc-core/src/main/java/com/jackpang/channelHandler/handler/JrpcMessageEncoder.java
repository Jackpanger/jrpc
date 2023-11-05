package com.jackpang.channelHandler.handler;

import com.jackpang.transport.message.JrpcRequest;
import com.jackpang.transport.message.MessageFormatConstant;
import com.jackpang.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 4bytes magic number --> jrpc.getBytes()
 * 1byte version --> 1
 * 2B header length
 * 4B full packet length
 * 1B SerializeType
 * 1B CompressType
 * 1B RequestType
 * 8B RequestId
 * body
 * description: first outbound handler
 * date: 11/4/23 11:41 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class JrpcMessageEncoder extends MessageToByteEncoder<JrpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, JrpcRequest jrpcRequest, ByteBuf byteBuf) throws Exception {
        // 4bytes magic number
        byteBuf.writeBytes(MessageFormatConstant.MAGIC_NUMBER);
        // 1byte version
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2B header length
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 4B full packet length
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 1B RequestType
        byteBuf.writeByte(jrpcRequest.getRequestType());
        // 1B SerializeType
        byteBuf.writeByte(jrpcRequest.getSerializeType());
        // 1B CompressType
        byteBuf.writeByte(jrpcRequest.getCompressType());
        // 8B RequestId
        byteBuf.writeLong(jrpcRequest.getRequestId());
        // body
        byte[] bodyBytes = getBodyBytes(jrpcRequest.getRequestPayload());
        byteBuf.writeBytes(bodyBytes);
        // store the index of the writer
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(7).writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBytes.length);
        // restore the index of the writer
        byteBuf.writerIndex(writerIndex);

    }

    private byte[] getBodyBytes(RequestPayload requestPayload) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(requestPayload);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("Serialization error:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
