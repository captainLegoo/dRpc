package com.dcy.listen;

import com.dcy.constant.ConnectConstant;
import com.dcy.util.JedisUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kyle
 * @date 2024/06/11
 * @description listen redis service address
 * Dynamic offline is automatically completed by the program's shutdown hook, no manual operation is required
 */
@Slf4j
public class ListenRedisServiceAddress implements Runnable{

    private final Map<String, List<InetSocketAddress>> serviceAddressMap;
    private final Map<String, List<InetSocketAddress>> pendingRemoveAddressMap;
    private final Map<String, List<InetSocketAddress>> pendingAddAddressMap;

    private final JedisUtils jedisUtils;

    public ListenRedisServiceAddress(Map<String, List<InetSocketAddress>> serviceAddressMap, Map<String, List<InetSocketAddress>> pendingRemoveAddressMap, Map<String, List<InetSocketAddress>> pendingAddAddressMap) {
        this.serviceAddressMap = serviceAddressMap;
        this.pendingRemoveAddressMap = pendingRemoveAddressMap;
        this.pendingAddAddressMap = pendingAddAddressMap;

        jedisUtils = new JedisUtils();
    }

    @Override
    public void run() {
        listenAddress();
    }

    private void listenAddress() {
        // remove offline address
        pendingRemoveAddressMap.forEach((serviceName, addressList) -> {
            String key = ConnectConstant.NAMESPACE + ConnectConstant.NODE_DEFAULT_PATH + serviceName;
            Set<String> serviceAddressSet = jedisUtils.sGet(key);

            if (serviceAddressSet != null && !serviceAddressSet.isEmpty()) {
                for (InetSocketAddress address : addressList) {
                    serviceAddressSet.remove(address);
                }
                if (serviceAddressSet.isEmpty()) {
                    jedisUtils.del(key);
                } else {
                    jedisUtils.sSet(key, serviceAddressSet.toArray(new String[0]));
                }
            }

        });

        // add new address
        serviceAddressMap.forEach((serviceName, addressList) -> {
            String key = ConnectConstant.NAMESPACE + ConnectConstant.NODE_DEFAULT_PATH + serviceName;
            Set<String> serviceAddressSet = jedisUtils.sGet(key);

            if (serviceAddressSet != null && !serviceAddressSet.isEmpty()) {
                // whether address was add
                List<InetSocketAddress> addressFromRedisList = serviceAddressSet.stream().map(address -> {
                    String[] split = address.split(":");
                    return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
                }).collect(Collectors.toList());

                for (InetSocketAddress address : addressFromRedisList) {
                    // add address: consumer cache does not contain address from redis
                    if (!addressList.contains(address)) {
                        // add to pending add address map
                        pendingAddAddressMap.computeIfAbsent(serviceName, k -> new ArrayList<>()).add(address);
                        log.debug("Address 【{}】 has been successfully added to the pending map cache...", address);
                    }
                }
            }
        });
    }
}