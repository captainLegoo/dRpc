package com.dcy.rpc.proxy;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
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
import java.util.concurrent.TimeUnit;

/**
 * @author Kyle
 * @date 2024/02/28
 */
@Slf4j
public class ConsumerInvocationHandler<T> implements InvocationHandler {
    private Class<T> interfaceRef;

    public ConsumerInvocationHandler(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // TODO set proxy object
        // 1. Encapsulate the message
        RequestPayload requestPayload = new RequestPayload()
                .setInterfaceName(interfaceRef.getName())
                .setMethodName(method.getName())
                .setParametersType(method.getParameterTypes())
                .setParameterValue(args)
                .setReturnType(method.getReturnType());

        GlobalConfig globalConfig = DRpcBootstrap.getInstance().getGlobalConfig();
        RequestProtocol requestProtocol = new RequestProtocol()
                .setRequestId(globalConfig.getIdGenerator().getId())
                .setRequestType(RequestTypeEnum.REQUEST.getId())
                .setCompressTypeId(globalConfig.getCompressType().getCompressId())
                .setSerializeTypeId(globalConfig.getSerializableType().getSerializeId())
                .setTimeStamp(new Date().getTime())
                .setRequestPayload(requestPayload);

        // 2.netty connection
        // 2.1.get address from registry center

        // 2.2.get available channel
        Channel channel = ConsumerNettyStarter.getNettyChannel("127.0.0.1", 9000);

        // 3.Store the request in a local thread

        // 4.Send a message
        ChannelFuture channelFuture = channel.writeAndFlush(ProtostuffUtil.serialize(requestProtocol)).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    log.info("send message failed");
                }
            }
        });

        // 5.Clean up threadLocal

        // 6.Get response result
        Object rs = channelFuture.get(5, TimeUnit.SECONDS);
        return rs;
    }
}
