package com.jackpang.channelHandler.handler;

import com.jackpang.JrpcBootstrap;
import com.jackpang.compress.Compressor;
import com.jackpang.compress.CompressorFactory;
import com.jackpang.serialize.Serializer;
import com.jackpang.serialize.SerializerFactory;
import com.jackpang.serialize.SerializerWrapper;
import com.jackpang.transport.message.JrpcRequest;
import com.jackpang.transport.message.MessageFormatConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;


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
public class JrpcRequestEncoder extends MessageToByteEncoder<JrpcRequest> {
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
        // 8B timestamp
        byteBuf.writeLong(jrpcRequest.getTimeStamp());
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
        // write into requestPayload
        byte[] bodyBytes = new byte[0];
        if (jrpcRequest.getRequestPayload() != null) {
            // 1 serialize requestPayload based on the configured serialization method

            Serializer serializer = SerializerFactory.getSerializer(JrpcBootstrap.getInstance().getConfiguration().getSerializeType()).getSerializer();
            bodyBytes = serializer.serialize(jrpcRequest.getRequestPayload());

            // 2. Compress the serialized requestPayload based on the configured compression method

            Compressor compressor = CompressorFactory.getCompressor(JrpcBootstrap.getInstance().getConfiguration().getCompressType()).getCompressor();
            bodyBytes = compressor.compress(bodyBytes);
            byteBuf.writeBytes(bodyBytes);
        }
        // store the index of the writer
        int writerIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageFormatConstant.MAGIC_NUMBER.length
                + MessageFormatConstant.VERSION_LENGTH +
                MessageFormatConstant.HEADER_FIELD_LENGTH).writeInt(MessageFormatConstant.HEADER_LENGTH + bodyBytes.length);
        // restore the index of the writer
        byteBuf.writerIndex(writerIndex);

        if (log.isDebugEnabled()) {
            log.debug("Request:{} finish packet encode in the client", jrpcRequest.getRequestId());
        }
    }


}
