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
     * select service address from cache
     * @return
     */
    InetSocketAddress selectServiceAddress();

    /**
     * select service address <p>
     * - create loadbalancer first time <p>
     * - reloadbalancer when lookupAddressList change
     * @param lookupAddressList
     * @return
     */
    InetSocketAddress selectServiceAddress(List<InetSocketAddress> lookupAddressList);

    void reloadBalance(List<InetSocketAddress> lookupAddressList);
}
