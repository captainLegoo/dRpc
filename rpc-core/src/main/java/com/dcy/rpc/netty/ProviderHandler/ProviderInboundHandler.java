package com.dcy.rpc.netty.ProviderHandler;

import com.dcy.rpc.cache.ProviderCache;
import com.dcy.rpc.config.ServiceConfig;
import com.dcy.rpc.entity.RequestPayload;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.entity.ResponseProtocol;
import com.dcy.rpc.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Objects;

/**
 * @author Kyle
 * @date 2024/02/24
 * <p>
 * Provider Message Decoder
 */
@Slf4j
public class ProviderInboundHandler extends SimpleChannelInboundHandler<Object> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        byte[] bytes = (byte[]) msg;
        RequestProtocol requestProtocol = ProtostuffUtil.deserialize(bytes, RequestProtocol.class);
        ctx.fireChannelRead(requestProtocol);
    }
}
