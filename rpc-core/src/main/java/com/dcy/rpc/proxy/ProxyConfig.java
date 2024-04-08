package com.dcy.rpc.proxy;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.cache.ProxyCache;
import com.dcy.rpc.config.GlobalConfig;
import com.dcy.rpc.entity.RequestProtocol;
import com.dcy.rpc.entity.RequestPayload;
import com.dcy.rpc.enumeration.RequestTypeEnum;
import com.dcy.rpc.netty.ConsumerNettyStarter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Kyle
 * @date 2024/02/27
 */
@AllArgsConstructor
@Slf4j
public class ProxyConfig<T> {
    private Class<T> interfaceRef;
    //private Registry registry;

    /**
     * Proxy design pattern, generates a proxy object
     * @return
     */
    public T get() {
        if (ProxyCache.PROXY_NAME_CACHE_SET.contains(interfaceRef.getName())) {
            return (T) ProxyCache.PROXY_OBJECT_CACHE_MAP.get(interfaceRef.getName());
        }

        ClassLoader classLoader =Thread.currentThread().getContextClassLoader();
        Class<T>[] classes = new Class[]{interfaceRef};

        Object proxyInstance = Proxy.newProxyInstance(classLoader, classes, new ConsumerInvocationHandler<T>(interfaceRef));
        log.info("【{}】 proxy is created.", interfaceRef.getName());

        // put proxy object to cache
        ProxyCache.PROXY_NAME_CACHE_SET.add(interfaceRef.getName());
        ProxyCache.PROXY_OBJECT_CACHE_MAP.put(interfaceRef.getName(), proxyInstance);

        // (Lazy) Once the proxy object is successfully created, the node can be dynamically detected online and offline.
        DRpcBootstrap.getInstance().getGlobalConfig().getRegistry().UpAndDownAddress(interfaceRef.getName());

        return (T) proxyInstance;
    }
}
