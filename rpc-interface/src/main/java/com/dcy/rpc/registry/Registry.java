package com.dcy.rpc.registry;

/**
 * @author Kyle
 * @date 2024/03/09
 * <p>
 * Registration center interface
 */
public interface Registry {
    /**
     * Register
     * @param serviceName
     * @param localIPAddress
     * @param port
     * @return
     */
    boolean register(String serviceName, String localIPAddress, int port);

    /**
     * Lookup
     * @param serviceName
     * @return
     */
    String lookupAddress(String serviceName);
}
