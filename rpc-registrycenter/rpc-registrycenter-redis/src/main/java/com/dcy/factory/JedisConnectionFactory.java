package com.dcy.factory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Kyle
 * @date 2024/05/24
 * @description Jedis connection factory
 */
public class JedisConnectionFactory {
    private static volatile JedisPool jedisPool;

    private static String host;
    private static int port;

    /** Private constructor to prevent instantiation */
    private JedisConnectionFactory() {}

    public static void setHost(String host) {
        if (jedisPool == null) {
            JedisConnectionFactory.host = host;
        } else {
            throw new IllegalStateException("JedisPool is already initialized.");
        }
    }

    public static void setPort(int port) {
        if (jedisPool == null) {
            JedisConnectionFactory.port = port;
        } else {
            throw new IllegalStateException("JedisPool is already initialized.");
        }
    }

    public static void initializePool() {
        if (jedisPool == null) {
            synchronized (JedisConnectionFactory.class) {
                if (jedisPool == null) {
                    // Configure connection pool
                    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
                    jedisPoolConfig.setMaxTotal(8);
                    jedisPoolConfig.setMaxIdle(8);
                    jedisPoolConfig.setMinIdle(0);
                    jedisPoolConfig.setMaxWaitMillis(1000);
                    // Create connection pool object
                    jedisPool = new JedisPool(jedisPoolConfig, host, port, 1000);
                }
            }
        }
    }

    public static Jedis getJedis() {
        if (jedisPool == null) {
            initializePool(); // Ensure the pool is initialized with set values or default values
        }
        return jedisPool.getResource();
    }
}