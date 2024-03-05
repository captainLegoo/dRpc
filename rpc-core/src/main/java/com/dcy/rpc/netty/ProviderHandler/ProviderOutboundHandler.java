package com.dcy.rpc.netty.ProviderHandler;

import com.dcy.rpc.compress.Compress;
import com.dcy.rpc.entity.ResponseProtocol;
import com.dcy.rpc.serialize.Serialize;
import com.dcy.rpc.strategy.CompressStrategy;
import com.dcy.rpc.strategy.SerializeStrategy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Kyle
 * @date 2024/02/29
 *
 * provider outbound handler
 */
@Slf4j
public class ProviderOutboundHandler extends MessageToByteEncoder<ResponseProtocol> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseProtocol responseProtocol, ByteBuf byteBuf) throws Exception {
        log.debug("response protocol:{}", responseProtocol);
        // request id
        long requestId = responseProtocol.getRequestId();
        byteBuf.writeLong(requestId);
        // response code
        byteBuf.writeByte(responseProtocol.getCode());
        // serialize type
        byte serializeTypeId = responseProtocol.getSerializeTypeId();
        byteBuf.writeByte(serializeTypeId);
        // compress type
        byte compressTypeId = responseProtocol.getCompressTypeId();
        byteBuf.writeByte(compressTypeId);
        // time stamp
        byteBuf.writeLong(responseProtocol.getTimeStamp());

        //  serialize response body
        Serialize serializer = SerializeStrategy.getSerializerById(serializeTypeId);
        byte[] serializered = serializer.serializer(responseProtocol.getResponseBody());

        // compress response body
        Compress compress = CompressStrategy.getCompressById(compressTypeId);
        byte[] compressed = compress.compress(serializered);

        byteBuf.writeBytes(compressed);
        ctx.writeAndFlush(byteBuf);
        log.debug("Provider send response, id is 【{}】", requestId);
    }
}
