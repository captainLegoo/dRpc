package com.dcy.rpc.loadbalancer;

import com.dcy.rpc.registry.Registry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author Kyle
 * @date 2023/10/2 15:43
 * <p>
 * interface for loadbalancer
 */
public interface Loadbalancer {

    /**
     * Select an available service address
     * @param lookupAddressList
     * @return
     */
    InetSocketAddress selectServiceAddress(List<InetSocketAddress>... lookupAddressList);
}
