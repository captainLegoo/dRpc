package com.dcy.rpc.netty.ConsumerHandler;

import com.dcy.rpc.entity.ResponseProtocol;
import com.dcy.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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
        System.out.println("response = " + responseProtocol.getResponseBody());
    }
}
