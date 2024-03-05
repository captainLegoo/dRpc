package com.dcy.rpc.netty.ConsumerHandler;

import com.dcy.rpc.compress.Compress;
import com.dcy.rpc.constant.MessageFormatConstant;
import com.dcy.rpc.entity.RequestPayload;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.enumeration.CompressTypeEnum;
import com.dcy.rpc.enumeration.SerializeTypeEnum;
import com.dcy.rpc.serialize.Serialize;
import com.dcy.rpc.strategy.CompressStrategy;
import com.dcy.rpc.strategy.SerializeStrategy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Kyle
 * @date 2024/03/04
 * <p>
 * Consumer Encode the request:
 * Converts a RequestProtocol object into a byte stream conforming
 * to the specified protocol format for transmission over the network
 * When popping from the stack, the first processor passed
 * <p>
 * MessageToByteEncoder:
 * Is an encoder class in Netty that is used to encode specific types of message objects into byte streams.
 * <p>
 * 4B magic
 * 1B version
 * 1B requestType
 * 1B serialize
 * 1B compress
 * 8B requestId
 * 8B time stamp
 * <p>
 * 0    1	 2	  3    4    5    6    7    8    9	 10	  11    12   13   14   15  16
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * |        magic     |ver | rt | ser|com |          requestId  				   |
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 * |																	           |
 * |										    	body	  					   |
 * |																		       |
 * +----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+----+
 */
@Slf4j
public class MsgToByteHandler extends MessageToByteEncoder<RequestProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, RequestProtocol requestProtocol, ByteBuf byteBuf) throws Exception {
        log.info("requestProtocol -> {}", requestProtocol);
        // magic
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // version
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // request type
        byteBuf.writeByte(requestProtocol.getRequestType());
        // serialize
        byte serializeType = requestProtocol.getSerializeType();
        byteBuf.writeByte(serializeType);
        // compress
        byte compressType = requestProtocol.getCompressType();
        byteBuf.writeByte(compressType);
        // requestId
        byteBuf.writeLong(requestProtocol.getRequestId());
        // time stamp
        byteBuf.writeLong(requestProtocol.getTimeStamp());

        RequestPayload requestPayload = requestProtocol.getRequestPayload();

        // serialize the payload
        Serialize serializer = SerializeStrategy.getSerializerById(serializeType);
        byte[] serializedPayload = serializer.serializer(requestPayload);

        // compress the payload
        Compress compress = CompressStrategy.getCompressById(compressType);
        byte[] compressedPayload = compress.compress(serializedPayload);

        // request body
        byteBuf.writeBytes(compressedPayload);

        // when finished, clear the buffer
        ctx.writeAndFlush(byteBuf).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.info("send request success");
                byteBuf.clear();
            }
        });
    }
}
