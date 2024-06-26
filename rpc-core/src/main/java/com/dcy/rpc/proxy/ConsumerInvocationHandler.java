package com.dcy.rpc.proxy;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.entity.RequestPayload;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.enumeration.RequestTypeEnum;
import com.dcy.rpc.loadbalancer.Loadbalancer;
import com.dcy.rpc.netty.ConsumerNettyStarter;
import com.dcy.rpc.strategy.LoadbalancerStrategy;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
                .setCompressType(globalConfig.getCompressType().getCompressId())
                .setSerializeType(globalConfig.getSerializableType().getSerializeId())
                .setTimeStamp(new Date().getTime())
                .setRequestPayload(requestPayload);

        // 2.get channel by address
        // 2.1.get available address from registry center and loadbalancer
        InetSocketAddress inetSocketAddress = getAvailableAddress(globalConfig);
        if (Objects.isNull(inetSocketAddress)) {
            throw new RuntimeException("No available address");
            //return null;
        }

        // 2.2.get available channel
        Channel channel = ConsumerNettyStarter.getNettyChannel(inetSocketAddress);

        // 3.create CompletableFuture and add to cache, Waiting to receive return information
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();

        channel.writeAndFlush(requestProtocol);
        ConsumerCache.FUTURES_NAP.put(requestId, completableFuture);

        // 4.get response result
        Object object = completableFuture.get(5, TimeUnit.SECONDS);

        // 5.remove future from cache
        ConsumerCache.FUTURES_NAP.remove(requestId);

        // 6.return result
        return object;
    }

    /**
     * get available address
     * @param globalConfig
     * @return
     */
    private InetSocketAddress getAvailableAddress(GlobalConfig globalConfig) {
        // get loadbalancer from cache
        Loadbalancer loadbalancer = ConsumerCache.LOADBALANCER_MAP.get(interfaceRef.getName());
        if (Objects.nonNull(loadbalancer)) {
            return loadbalancer.selectServiceAddress();
        }

        // create loadbalancer
        List<InetSocketAddress> inetSocketAddressList = globalConfig.getRegistry().lookupAllAddress(interfaceRef.getName());
        if (inetSocketAddressList.isEmpty()) {
            return null;
        }
        ConsumerCache.SERVICE_ADDRESS_MAP.put(interfaceRef.getName(), inetSocketAddressList);
        loadbalancer = LoadbalancerStrategy.getLoadbalancer(globalConfig.getLoadbalancerTypeEnum());
        // put loadbalancer to cache
        ConsumerCache.LOADBALANCER_MAP.put(interfaceRef.getName(), loadbalancer);
        return loadbalancer.selectServiceAddress(inetSocketAddressList);
    }
}
