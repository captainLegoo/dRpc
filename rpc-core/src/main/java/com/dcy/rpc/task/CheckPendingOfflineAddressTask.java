package com.dcy.rpc.task;

import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.cache.NettyCache;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kyle
 * @date 2024/05/17
 * @description Check for addresses to be removed
 */
@Slf4j
public class CheckPendingOfflineAddressTask implements Runnable{
    @Override
    public void run() {
        NettyCache.CHANNEL_MAP.forEach((address, channel) -> {
            if (!channel.isActive() && !channel.isRegistered() && !channel.isOpen()) {
                ConsumerCache.SERVICE_ADDRESS_MAP.forEach((serviceName, inetSocketAddressList) -> {
                    boolean isRemove = inetSocketAddressList.removeIf(inetSocketAddress -> inetSocketAddress.equals(address));
                    if (isRemove) {
                        List<InetSocketAddress> addressList = NettyCache.PENDING_REMOVE_ADDRESS_MAP.computeIfAbsent(serviceName, k -> new ArrayList<>());
                        addressList.add(address);
                        ConsumerCache.LOADBALANCER_MAP.get(serviceName).reloadBalance(inetSocketAddressList);
                        log.info("The service address {} has been removed from the cache", address);
                    }
                });
            }
        });
    }
}
