package com.dcy.rpc.netty.ProviderHandler;

import com.dcy.rpc.compress.Compress;
import com.dcy.rpc.entity.RequestPayload;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.enumeration.RequestTypeEnum;
import com.dcy.rpc.serialize.Serialize;
import com.dcy.rpc.strategy.CompressStrategy;
import com.dcy.rpc.strategy.SerializeStrategy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Kyle
 * @date 2024/02/24
 * <p>
 * Provider Message Decoder
 */
@Slf4j
public class ProviderInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        RequestProtocol requestProtocol = new RequestProtocol();
        // magic
        byte[] magicByte = new byte[4];
        byteBuf.readBytes(magicByte);
        String magicString = new String(magicByte);
        // version
        byte versionByte = byteBuf.readByte();
        // request type
        byte requestTypeByte = byteBuf.readByte();
        // serialize type
        byte serializeByte = byteBuf.readByte();
        // compress type
        byte compressByte = byteBuf.readByte();
        // request id
        long requestId = byteBuf.readLong();
        // time stamp
        long timeStamp = byteBuf.readLong();

        requestProtocol.setRequestId(requestId)
                .setRequestType(requestTypeByte)
                .setSerializeType(serializeByte)
                .setCompressType(compressByte);

        if (requestTypeByte == RequestTypeEnum.HEART.getId()) {
            log.debug("Provider receive request, id is 【{}】", requestId);
            // pass next handler
            ctx.fireChannelRead(requestProtocol);
        }
        // request payload
        int bodyLength = byteBuf.writerIndex() - byteBuf.readerIndex();
        byte[] bodyByte = new byte[bodyLength];
        byteBuf.readBytes(bodyByte);
        // unzip the payload
        Compress compress = CompressStrategy.getCompressById(compressByte);
        bodyByte = compress.decompress(bodyByte);
        //  deserialize payload
        Serialize serializer = SerializeStrategy.getSerializerById(serializeByte);
        RequestPayload requestPayload = serializer.deserialize(bodyByte, RequestPayload.class);

        //RequestProtocol requestProtocol = RequestProtocol.builder()
        //        .requestId(requestId)
        //        .requestType(requestTypeByte)
        //        .serializeType(serializeByte)
        //        .compressType(compressByte)
        //        .requestPayload(requestPayload)
        //        .build();

        requestProtocol.setRequestPayload(requestPayload);


        log.debug("Provider receive request, id is 【{}】", requestId);

        // pass next handler
        ctx.fireChannelRead(requestProtocol);
    }
}
