package com.dcy.rpc.netty;

import com.dcy.rpc.cache.NettyCache;
import com.dcy.rpc.netty.ConsumerHandler.ConsumerInBoundHandler;
import com.dcy.rpc.netty.ConsumerHandler.MsgToByteHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
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

    private static final Bootstrap bootstrap = new Bootstrap();

    static {
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                        //pipeline.addLast(new ObjectEncoder());
                        //pipeline.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        pipeline.addLast(new MsgToByteHandler());
                        pipeline.addLast(new ConsumerInBoundHandler());
                    }
                });
    }

    public static Channel getNettyChannel(String host, int port) {
        InetSocketAddress address = new InetSocketAddress(host, port);
        return getNettyChannel(address);
    }

    public static Channel getNettyChannel(InetSocketAddress address) {

        Channel channel = NettyCache.CHANNEL_MAP.get(address);
        if (channel != null && channel.isActive()) {
            log.debug("The channel is still active -> 【{}】", channel.remoteAddress());
            return channel;
        }

        // If the channel is invalid or does not exist, remove it from the cache
        if (channel != null) {
            NettyCache.CHANNEL_MAP.remove(address);
        }

        CompletableFuture<Channel> completableFutureChannel = new CompletableFuture<>();
        bootstrap.connect(address).addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                log.info("Connection has been successfully established with network 【{}】.", address);
                Channel newChannel = channelFuture.channel();
                NettyCache.CHANNEL_MAP.put(address, newChannel);
                completableFutureChannel.complete(newChannel);
            } else {
                completableFutureChannel.completeExceptionally(channelFuture.cause());
            }
        });

        try {
            channel = completableFutureChannel.get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.debug("An exception occurred while acquiring a channel.");
            NettyCache.CHANNEL_MAP.remove(address);
            //throw new ChannelException("Failed to acquire channel", e);
        }

        if (channel == null) {
            log.error("An exception occurred while acquiring or establishing a channel with 【{}】.", address);
            //throw new ChannelException("An exception occurred while getting the channel channel.");
        }


        return channel;
    }
}
