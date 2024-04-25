package com.dcy.rpc.loadbalancer.impl;

import com.dcy.rpc.loadbalancer.Loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Kyle
 * @date 2024/03/19
 * <p>
 * round loadbalancer
 */
public class RoundRobinLoadbalancer implements Loadbalancer {
    private List<InetSocketAddress> addressList = new CopyOnWriteArrayList<>();
    private final AtomicInteger atomicInteger = new AtomicInteger(0);

    @Override
    public InetSocketAddress selectServiceAddress() {
        return selectServiceAddress(addressList);
    }

    @Override
    public InetSocketAddress selectServiceAddress(List<InetSocketAddress> lookupAddressList) {
        if (addressList.isEmpty()) {
            addressList = lookupAddressList;
        }
        //int index = atomicInteger.getAndIncrement() % addressList.size();
        //return addressList.get(index);
        if (atomicInteger.get() >= addressList.size()) {
            atomicInteger.set(0);
        }
        return addressList.get(atomicInteger.getAndIncrement());
    }

    @Override
    public void reloadBalance(List<InetSocketAddress> lookupAddressList) {
        addressList = lookupAddressList;
    }
}
