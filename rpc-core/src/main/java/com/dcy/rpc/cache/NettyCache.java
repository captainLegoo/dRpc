package com.dcy.rpc.cache;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kyle
 * @date 2024/04/08
 * <p>
 * storage netty cahce
 */
public class NettyCache {
    public static final Map<InetSocketAddress, Channel> CHANNEL_MAP = new HashMap<>();

    public static final Map<String, List<InetSocketAddress>> PENDING_REMOVE_ADDRESS_MAP = new ConcurrentHashMap<>();
    public static final Map<String, List<InetSocketAddress>> PENDING_ADD_ADDRESS_MAP = new ConcurrentHashMap<>();
}
