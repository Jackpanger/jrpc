package com.jackpang.channelHandler.handler;

import com.jackpang.enumeration.RequestType;
import com.jackpang.serialize.Serializer;
import com.jackpang.serialize.SerializerFactory;
import com.jackpang.transport.message.JrpcRequest;
import com.jackpang.transport.message.MessageFormatConstant;
import com.jackpang.transport.message.RequestPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * description: frame decoder based on length field
 * date: 11/5/23 9:14 AM
 * author: jinhao_pang
 * version: 1.0
 */
@Slf4j
public class JrpcRequestDecoder extends LengthFieldBasedFrameDecoder {
    public JrpcRequestDecoder() {
        super(
                // max frame length
                MessageFormatConstant.MAX_FRAME_LENGTH,
                // length field offset
                MessageFormatConstant.MAGIC_NUMBER.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH,
                // length field length
                MessageFormatConstant.FULL_FIELD_LENGTH,
                // length adjustment  total length
                -(MessageFormatConstant.MAGIC_NUMBER.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_FIELD_LENGTH + MessageFormatConstant.FULL_FIELD_LENGTH),
                // initial bytes to strip
                0
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf byteBuf) {
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        // 1.  read magic number
        byte[] magic = new byte[MessageFormatConstant.MAGIC_NUMBER.length];
        byteBuf.readBytes(magic);
        // check if the magic number is correct
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC_NUMBER[i]) {
                throw new RuntimeException("magic number in request is illegal");
            }
        }
        // read version
        byte version = byteBuf.readByte();
        if (version != MessageFormatConstant.VERSION) {
            throw new RuntimeException("version in request is illegal");
        }
        // read header length
        short header = byteBuf.readShort();

        // read full length
        int fullLength = byteBuf.readInt();
        // read request type
        byte requestType = byteBuf.readByte();
        // read serialize type
        byte serializeType = byteBuf.readByte();
        // read compress type
        byte compressType = byteBuf.readByte();
        // rad request id
        long requestId = byteBuf.readLong();
        // get body
        JrpcRequest jrpcRequest = new JrpcRequest();
        jrpcRequest.setRequestId(requestId);
        jrpcRequest.setCompressType(compressType);
        jrpcRequest.setSerializeType(serializeType);
        jrpcRequest.setRequestType(requestType);

        // directly return: heartbeat request
        if (requestType == RequestType.HEARTBEAT.getId())
            return jrpcRequest;
        int payload = fullLength - header;
        byte[] payLoad = new byte[payload];
        byteBuf.readBytes(payLoad);

        // decompression

        // deserialization

        Serializer serializer = SerializerFactory.getSerializer(serializeType).getSerializer();
        RequestPayload requestPayload = serializer.deserialize(payLoad, RequestPayload.class);
        jrpcRequest.setRequestPayload(requestPayload);
        if (log.isDebugEnabled()) {
            log.debug("Request:{} finish packet decode in the server", jrpcRequest.getRequestId());
        }

        return jrpcRequest;
    }
}
