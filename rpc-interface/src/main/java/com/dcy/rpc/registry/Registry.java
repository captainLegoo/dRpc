package com.dcy.rpc.registry;

/**
 * @author Kyle
 * @date 2024/03/09
 * <p>
 * Registration center interface
 */
public interface Registry {
    boolean register(String serviceName, String localIPAddress, int port);
}
