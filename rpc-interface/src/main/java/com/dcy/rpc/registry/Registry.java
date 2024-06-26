package com.dcy.rpc.registry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Kyle
 * @date 2024/03/09
 * <p>
 * Registration center interface
 */
public interface Registry {
    /**
     * Register a service
     * @param serviceName
     * @param localIPAddress
     * @param port
     * @return
     */
    boolean register(String serviceName, String localIPAddress, int port);

    /**
     * Lookup an address
     * @param serviceName
     * @return
     */
    String lookupAddress(String serviceName);

    /**
     * Lookup all address
     * @param serviceName
     * @return
     */
    List<InetSocketAddress> lookupAllAddress(String serviceName);

    /**
     * Close program execution
     * @param serverAddressMap map of service address
     */
    void closeProgramAction(Map<String, List<InetSocketAddress>> serverAddressMap);
}
