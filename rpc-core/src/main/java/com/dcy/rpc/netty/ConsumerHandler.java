package com.dcy.rpc.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Kyle
 * @date 2024/02/24
 */
@Slf4j
public class ConsumerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        log.info("Consumer get msg -> {}", msg);
    }
}
