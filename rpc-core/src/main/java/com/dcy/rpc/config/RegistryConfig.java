package com.dcy.rpc.config;

/**
 * @author Kyle
 * @date 2024/02/20
 */
public class RegistryConfig {
    private String registryAddress;
    private String registryCenter;
    private String host;
    private int port;

    public RegistryConfig(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public RegistryConfig(String registryCenter, String host, int port) {
        this.registryCenter = registryCenter;
        this.host = host;
        this.port = port;
    }
}
