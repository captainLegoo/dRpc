package com.dcy.rpc.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author Kyle
 * @date 2024/02/22
 */
public class ConsumerNettyStarter {

    public static Channel getNettyChannel(String host, int port) {
        InetSocketAddress address = new InetSocketAddress(host, port);
        return getNettyChannel(address);
    }

    public static Channel getNettyChannel(InetSocketAddress address) {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(address).sync();
            channelFuture.channel().closeFuture().sync();
            return channelFuture.channel();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            eventExecutors.shutdownGracefully();
        }
        return null;
    }
}
