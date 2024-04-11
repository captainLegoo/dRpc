package com.dcy.rpc.cache;

import org.apache.curator.framework.CuratorFramework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kyle
 * @date 2024/04/07
 */
public class ZkpCache {
    // cache for zkp client  key:address(127.0.0.1:2181)  value:client object
    public static final Map<String, CuratorFramework> CLIENT_CACHE = new ConcurrentHashMap<>(8);
}
