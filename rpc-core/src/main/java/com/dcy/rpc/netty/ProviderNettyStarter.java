package com.dcy.rpc.netty;

import com.dcy.rpc.netty.ProviderHandler.MethodCallHandler;
import com.dcy.rpc.netty.ProviderHandler.ProviderInboundHandler;
import com.dcy.rpc.netty.ProviderHandler.ProviderOutboundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Kyle
 * @date 2024/02/21
 *
 * provider netty start
 */
@Slf4j
public class ProviderNettyStarter {
    private int port;

    public ProviderNettyStarter(int port) {
        this.port = port;
    }

    public void start() {
        NioEventLoopGroup boosGroup = new NioEventLoopGroup(5);
        NioEventLoopGroup workGroup = new NioEventLoopGroup(10);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                            pipeline.addLast(new ObjectEncoder());
                            pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new ProviderInboundHandler());
                            pipeline.addLast(new MethodCallHandler());
                            pipeline.addLast(new ProviderOutboundHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            log.info("service provider start");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
