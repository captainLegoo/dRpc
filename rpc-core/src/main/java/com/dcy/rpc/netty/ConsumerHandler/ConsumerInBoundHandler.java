package com.dcy.rpc.netty.ConsumerHandler;

import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.compress.Compress;
import com.dcy.rpc.enumeration.RequestTypeEnum;
import com.dcy.rpc.serialize.Serialize;
import com.dcy.rpc.strategy.CompressStrategy;
import com.dcy.rpc.strategy.SerializeStrategy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author Kyle
 * @date 2024/02/24
 */
@Slf4j
public class ConsumerInBoundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) throws Exception {
        // request id
        long requestId = byteBuf.readLong();
        log.debug("ConsumerInBoundHandler receive response, id is 【{}】", requestId);
        // request type
        byte requestType = byteBuf.readByte();
        // response code
        byte code = byteBuf.readByte();
        // serialize Type Id
        byte serializeTypeId = byteBuf.readByte();
        // compress Type Id
        byte compressTypeId = byteBuf.readByte();
        // time stamp
        long timeStamp = byteBuf.readLong();

        Object responseBody = null;
        if (requestType == RequestTypeEnum.REQUEST.getId()) {
            // response body
            int responseBodyLength = byteBuf.writerIndex() - byteBuf.readerIndex();
            byte[] responseBodyByte = new byte[responseBodyLength];
            byteBuf.readBytes(responseBodyByte);

            // decompress the response body
            Compress compress = CompressStrategy.getCompressById(compressTypeId);
            responseBodyByte = compress.decompress(responseBodyByte);

            // deserialize the response body
            Serialize serializer = SerializeStrategy.getSerializerById(serializeTypeId);
            responseBody = serializer.deserialize(responseBodyByte, Object.class);
        } else {
            responseBody = "Heart beat";
        }

        // get completableFuture from cache
        CompletableFuture<Object> completableFuture = ConsumerCache.FUTURES_NAP.get(requestId);
        // 1==success
        if (code == 1) {
            log.debug("Id:【{}】 get the remote calling result successfully.", requestId);
            completableFuture.complete(responseBody);
        } else {
            completableFuture.complete(null);
        }
        // remove completableFuture
        ConsumerCache.FUTURES_NAP.remove(requestId);
    }
}
