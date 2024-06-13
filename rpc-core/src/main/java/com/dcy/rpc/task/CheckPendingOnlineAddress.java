package com.dcy.rpc.task;

import com.dcy.rpc.cache.ConsumerCache;
import com.dcy.rpc.cache.NettyCache;
import com.dcy.rpc.loadbalancer.Loadbalancer;
import com.dcy.rpc.netty.ConsumerNettyStarter;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kyle
 * @date 2024/05/17
 * @description Check the address to be launched
 */
@Slf4j
public class CheckPendingOnlineAddress implements Runnable {
    @Override
    public void run() {
        log.debug("CheckPendingOnlineAddress start");
        detectAddress();
    }

    private static void detectAddress() {
        NettyCache.PENDING_ADD_ADDRESS_MAP.forEach((serviceName, inetSocketAddressList) -> {
            List<InetSocketAddress> addressList = ConsumerCache.SERVICE_ADDRESS_MAP.computeIfAbsent(serviceName, k -> new ArrayList<>());

            // Iterate over each address in PENDING_ADD_ADDRESS_MAP and process them
            Iterator<InetSocketAddress> iterator = inetSocketAddressList.iterator();
            while (iterator.hasNext()) {
                InetSocketAddress address = iterator.next();
                Channel channel = ConsumerNettyStarter.getNettyChannel(address);
                log.info("HeartbeatDetectionTask checking channel -> {}", channel);

                // If the address is not in addressList, add it and reload the load balancer if it exists
                if (!addressList.contains(address)) {
                    addressList.add(address);
                    Loadbalancer loadbalancer = ConsumerCache.LOADBALANCER_MAP.get(serviceName);
                    if (loadbalancer != null) {
                        loadbalancer.reloadBalance(addressList);
                    }
                    log.debug("HeartbeatDetectionTask reloadBalance addressList -> {}", addressList);

                    // Remove processed addresses from PENDING_ADD_ADDRESS_MAP
                    iterator.remove();
                }
            }

            // If the list in PENDING_ADD_ADDRESS_MAP is empty, delete the entire entry
            if (inetSocketAddressList.isEmpty()) {
                NettyCache.PENDING_ADD_ADDRESS_MAP.remove(serviceName);
            }
        });
    }
}
