package com.dcy.rpc.cache;

import com.dcy.rpc.config.ServiceConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kyle
 * @date 2024/02/29
 */
public class ProviderCache {
    public static final Map<String, ServiceConfig<?>> SERVERS_LIST = new HashMap<>(16);
}
