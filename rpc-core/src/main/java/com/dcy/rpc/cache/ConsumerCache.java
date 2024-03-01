package com.dcy.rpc.cache;

import java.util.HashMap;
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
}
