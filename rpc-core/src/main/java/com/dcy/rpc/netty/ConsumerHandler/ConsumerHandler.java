package com.dcy.rpc.netty.ConsumerHandler;

import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.entity.ResponseProtocol;
import com.dcy.rpc.util.ProtostuffUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * @author Kyle
 * @date 2024/02/24
 */
@Slf4j
public class ConsumerHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] bytes = (byte[]) msg;
        ResponseProtocol responseProtocol = ProtostuffUtil.deserialize(bytes, ResponseProtocol.class);
        // get request id
        long requestId = responseProtocol.getRequestId();
        // get completableFuture from cache
        CompletableFuture<Object> completableFuture = ConsumerCache.FUTURES_NAP.get(requestId);
        // 1==success
        if (responseProtocol.getCode() == 1) {
            log.info("Id:【{}】 get the remote calling result.", requestId);
            completableFuture.complete(responseProtocol.getResponseBody());
        } else {
            completableFuture.complete(null);
        }
        // remove completableFuture
        ConsumerCache.FUTURES_NAP.remove(requestId);
    }
}
