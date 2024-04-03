package com.dcy.rpc.cache;

import com.dcy.rpc.loadbalancer.Loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kyle
 * @date 2024/03/01
 * <p>
 * consumer cache
 */
public class ConsumerCache {
    // Caching of request id and CompletableFuture
    public static final Map<Long, CompletableFuture<Object>> FUTURES_NAP = new ConcurrentHashMap<>(16);

    // Caching of service name and load balancing
    public static final Map<String, Loadbalancer> LOADBALANCER_MAP = new ConcurrentHashMap<>(16);

    // Caching of service name and service address
    public static final Map<String, List<InetSocketAddress>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>(16);
}
