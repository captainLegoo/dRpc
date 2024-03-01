package com.dcy.rpc.netty.ProviderHandler;

import com.dcy.rpc.entity.ResponseProtocol;
import com.dcy.rpc.util.ProtostuffUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Kyle
 * @date 2024/02/29
 *
 * provider outbound handler
 */
@Slf4j
public class ProviderOutboundHandler extends SimpleChannelInboundHandler<ResponseProtocol>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ResponseProtocol responseProtocol) throws Exception {
        log.info("ProviderOutboundHandler receive return msg");
        ctx.writeAndFlush(ProtostuffUtil.serialize(responseProtocol));
        log.info("response is successful send");
    }
}
