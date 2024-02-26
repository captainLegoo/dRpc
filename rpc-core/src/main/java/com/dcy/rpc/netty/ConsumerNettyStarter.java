package com.dcy.rpc.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Kyle
 * @date 2024/02/22
 *
 * consumer netty starter
 */
@Slf4j
public class ConsumerNettyStarter {

    private static final Map<InetSocketAddress, Channel> CHANNEL_MAP = new HashMap<>();

    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new StringDecoder());
                        pipeline.addLast(new StringEncoder());
                        pipeline.addLast(new ConsumerHandler());
                    }
                });
    }

    public static Channel getNettyChannel(String host, int port) {
        InetSocketAddress address = new InetSocketAddress(host, port);
        return getNettyChannel(address);
    }

    public static Channel getNettyChannel(InetSocketAddress address) {

        Channel channel = CHANNEL_MAP.get(address);
        if (channel != null) {
            return channel;
        }
        CompletableFuture<Channel> completableFutureChannel = new CompletableFuture<>();
        bootstrap.connect(address).addListener((ChannelFutureListener) channelFuture -> {
            boolean success = channelFuture.isSuccess();
            if (success) {
                log.info("Connection has been successfully established with network 【{}】.", address);
                completableFutureChannel.complete(channelFuture.channel());
            }
        });

        try {
            channel = completableFutureChannel.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.debug("An exception occurred while acquiring a channel.");
            throw new ChannelException(e);
        }

        if (channel == null) {
            log.error("An exception occurred while acquiring or establishing a channel with 【{}】.", address);
            throw new ChannelException("An exception occurred while getting the channel channel.");
        }

        CHANNEL_MAP.put(address, channel);

        return channel;
    }
}
