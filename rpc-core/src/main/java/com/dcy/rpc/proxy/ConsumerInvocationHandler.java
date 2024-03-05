package com.dcy.rpc.proxy;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.entity.RequestPayload;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.enumeration.RequestTypeEnum;
import com.dcy.rpc.netty.ConsumerNettyStarter;
import com.dcy.rpc.util.ProtostuffUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Kyle
 * @date 2024/02/28
 */
@Slf4j
public class ConsumerInvocationHandler<T> implements InvocationHandler {
    private final Class<T> interfaceRef;

    public ConsumerInvocationHandler(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. Encapsulate the message
        RequestPayload requestPayload = new RequestPayload()
                .setInterfaceName(interfaceRef.getName())
                .setMethodName(method.getName())
                .setParametersType(method.getParameterTypes())
                .setParameterValue(args)
                .setReturnType(method.getReturnType());

        GlobalConfig globalConfig = DRpcBootstrap.getInstance().getGlobalConfig();
        long requestId = globalConfig.getIdGenerator().getId();
        RequestProtocol requestProtocol = new RequestProtocol()
                .setRequestId(requestId)
                .setRequestType(RequestTypeEnum.REQUEST.getId())
                .setCompressType(globalConfig.getCompressType())
                .setSerializeType(globalConfig.getSerializableType())
                .setTimeStamp(new Date().getTime())
                .setRequestPayload(requestPayload);

        // TODO get address from registry center
        // 2.netty connection
        // 2.1.get address from registry center

        // 2.2.get available channel
        Channel channel = ConsumerNettyStarter.getNettyChannel("127.0.0.1", 9000);

        // 3.create CompletableFuture and add to cache, Waiting to receive return information
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();

        //

        //
        channel.writeAndFlush(requestProtocol).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    log.info("Id:【{}】 send message failed", requestId);
                } else {
                    log.info("Id:【{}】 send message success", requestId);
                    ConsumerCache.FUTURES_NAP.put(requestId, completableFuture);
                }
            }
        });

        // 4.get response result
        return completableFuture.get(5, TimeUnit.SECONDS);
    }
}
