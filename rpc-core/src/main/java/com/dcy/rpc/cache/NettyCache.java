package com.dcy.rpc.cache;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kyle
 * @date 2024/04/08
 * <p>
 * storage netty cahce
 */
public class NettyCache {
    public static final Map<InetSocketAddress, Channel> CHANNEL_MAP = new HashMap<>();
}