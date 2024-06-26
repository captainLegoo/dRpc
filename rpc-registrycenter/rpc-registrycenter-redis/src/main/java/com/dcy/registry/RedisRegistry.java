package com.dcy.registry;

import com.dcy.constant.ConnectConstant;
import com.dcy.factory.JedisConnectionFactory;
import com.dcy.rpc.registry.Registry;
import com.dcy.util.JedisUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        return jedisUtils.sHasKey(key, value) || jedisUtils.sSet(key, value) == 1;
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

    @Override
    public void closeProgramAction(Map<String, List<InetSocketAddress>> serverAddressMap) {
        serverAddressMap.forEach((serviceName, inetSocketAddressList) -> {
            String key = ConnectConstant.NAMESPACE + ConnectConstant.NODE_DEFAULT_PATH + serviceName;
            List<String> addresses = new ArrayList<>();

            inetSocketAddressList.forEach(inetSocketAddress ->
                    addresses.add(inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort())
            );

            // Delete addresses in batches
            jedisUtils.setRemove(key, addresses.toArray(new String[0]));

            // If the collection is empty, delete the key
            if (jedisUtils.sGetSetSize(key) == 0) {
                jedisUtils.del(key);
            }
        });
    }
}
