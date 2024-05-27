package com.dcy.rpc.cache;

import com.dcy.rpc.config.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kyle
 * @date 2024/02/29
 * <p>
 * provider ache
 */
public class ProviderCache {
    // Service publish cache
    public static final Map<String, ServiceConfig<?>> SERVERS_MAP = new ConcurrentHashMap<>(16);
    public static final Map<String, List<InetSocketAddress>> SERVERS_ADDRESS_MAP = new ConcurrentHashMap<>(16);
}
