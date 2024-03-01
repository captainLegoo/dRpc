package com.dcy.rpc.cache;

import com.dcy.rpc.config.ServiceConfig;

import java.util.HashMap;
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
    public static final Map<String, ServiceConfig<?>> SERVERS_LIST = new ConcurrentHashMap<>(16);
}
