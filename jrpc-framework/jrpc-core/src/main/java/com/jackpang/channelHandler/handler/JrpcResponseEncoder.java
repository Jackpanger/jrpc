package com.jackpang.channelHandler.handler;

import com.jackpang.compress.Compressor;
import com.jackpang.compress.CompressorFactory;
import com.jackpang.serialize.Serializer;
import com.jackpang.serialize.SerializerFactory;
import com.jackpang.transport.message.JrpcRequest;
import com.jackpang.transport.message.JrpcResponse;
import com.jackpang.transport.message.MessageFormatConstant;
import com.jackpang.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 4bytes magic number --> jrpc.getBytes()
 * 1byte version --> 1
 * 2B header length
 * 4B full packet length
 * 1B SerializeType
 * 1B CompressType
 * 1B code
 * 8B RequestId
 * object
 * description: first outbound handler
 * date: 11/4/23 11:41 PM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class JrpcResponseEncoder extends MessageToByteEncoder<JrpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, JrpcResponse jrpcResponse, ByteBuf byteBuf) throws Exception {
        // 4bytes magic number
        byteBuf.writeBytes(MessageFormatConstant.MAGIC_NUMBER);
        // 1byte version
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 2B header length
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);
        // 4B full packet length
        byteBuf.writerIndex(byteBuf.writerIndex() + MessageFormatConstant.FULL_FIELD_LENGTH);
        // 1B RequestType
        byteBuf.writeByte(jrpcResponse.getCode());
        // 1B SerializeType
        byteBuf.writeByte(jrpcResponse.getSerializeType());
        // 1B CompressType
        byteBuf.writeByte(jrpcResponse.getCompressType());
        // 8B RequestId
        byteBuf.writeLong(jrpcResponse.getRequestId());
        // 8B timestamp
        byteBuf.writeLong(jrpcResponse.getTimeStamp());
        // if it is a heartbeat request, then there is no body.
//        if (jrpcRequest.getRequestType() == RequestType.HEARTBEAT.getId()) {
//            // store the index of the writer
//            int writerIndex = byteBuf.writerIndex();
//            byteBuf.writerIndex(MessageFormatConstant.MAGIC_NUMBER.length
//                    +MessageFormatConstant.VERSION_LENGTH+
//                    MessageFormatConstant.HEADER_FIELD_LENGTH).writeInt(MessageFormatConstant.HEADER_LENGTH);
//            // restore the index of the writer
//            byteBuf.writerIndex(writerIndex);
//            return;
//        }
        byte[] bodyBytes = new byte[0];
        if (jrpcResponse.getBody() != null) {
            // body serialize
            Serializer serializer = SerializerFactory.getSerializer(jrpcResponse.getSerializeType()).getSerializer();
            bodyBytes = serializer.serialize(jrpcResponse.getBody());

            // compress
            Compressor compressor = CompressorFactory.getCompressor(jrpcResponse.getCompressType()).getCompressor();
            bodyBytes = compressor.compress(bodyBytes);
        }

        byteBuf.writeBytes(bodyBytes);
        // store the index of the writer
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageFormatConstant.MAGIC_NUMBER.length
                + MessageFormatConstant.VERSION_LENGTH +
                MessageFormatConstant.HEADER_FIELD_LENGTH).writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBytes.length);
        // restore the index of the writer
        byteBuf.writerIndex(writerIndex);
        if (log.isDebugEnabled()) {
            log.debug("Encode response:{} in the server", jrpcResponse.getRequestId());
        }
    }
}
