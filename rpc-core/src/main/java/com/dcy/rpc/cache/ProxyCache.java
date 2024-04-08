package com.dcy.rpc.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Kyle
 * @date 2024/04/08
 */
public class ProxyCache {
    // cache service Name and proxy object
    public static final Map<String, Object> PROXY_OBJECT_CACHE_MAP = new HashMap<>(16);

    // cache service Name
    public static final Set<String> PROXY_NAME_CACHE_SET = new HashSet<>(16);
}
