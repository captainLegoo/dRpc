package com.dcy.registry;

import com.dcy.constant.ConnectConstant;
import com.dcy.factory.JedisConnectionFactory;
import com.dcy.rpc.registry.Registry;
import com.dcy.util.JedisUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Kyle
 * @date 2024/05/25
 * @description connect and publish service in Redis
 */
public class RedisRegistry implements Registry {

    private final JedisUtils jedisUtils;
    private final String address;
    private final int port;

    public RedisRegistry(String address, int port) {
        this.address = address;
        this.port = port;
        JedisConnectionFactory.setHost(address);
        JedisConnectionFactory.setPort(port);

        JedisConnectionFactory.initializePool();

        jedisUtils = new JedisUtils();
    }

    @Override
    public boolean register(String serviceName, String localIPAddress, int port) {
        String key = ConnectConstant.NAMESPACE + ConnectConstant.NODE_DEFAULT_PATH + serviceName;
        String value = localIPAddress + ":" + port;

        if (jedisUtils.sSet(key, value) == 1) {
            return true;
        }

        return false;
    }

    @Override
    public String lookupAddress(String serviceName) {
        String key = ConnectConstant.NAMESPACE + ConnectConstant.NODE_DEFAULT_PATH + serviceName;
        Set<String> serviceAddressSet = jedisUtils.sGet(key);

        if (serviceAddressSet != null && !serviceAddressSet.isEmpty()) {
            return serviceAddressSet.iterator().next();
        }

        return null;
    }

    @Override
    public List<InetSocketAddress> lookupAllAddress(String serviceName) {
        String key = ConnectConstant.NAMESPACE + ConnectConstant.NODE_DEFAULT_PATH + serviceName;
        Set<String> serviceAddressSet = jedisUtils.sGet(key);
        if (serviceAddressSet != null && !serviceAddressSet.isEmpty()) {
            List<InetSocketAddress> inetSocketAddressList = new ArrayList<>();
            serviceAddressSet.forEach(address -> {
                String[] addressArray = address.split(":");
                inetSocketAddressList.add(new InetSocketAddress(addressArray[0], Integer.parseInt(addressArray[1])));
            });
            return inetSocketAddressList;
        }
        return null;
    }
}
