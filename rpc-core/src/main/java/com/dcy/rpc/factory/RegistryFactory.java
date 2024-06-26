package com.dcy.rpc.factory;

import com.dcy.registry.RedisRegistry;
import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.cache.ProviderCache;
import com.dcy.rpc.config.RegistryConfig;
import com.dcy.rpc.enumeration.RegistryCenterEnum;
import com.dcy.rpc.registry.Registry;
import com.dcy.rpc.registry.ZookeeperRegistry;

import java.util.Objects;

/**
 * @author Kyle
 * @date 2024/03/09
 * <p>
 * Use a simple factory method to obtain the registration center
 */
public class RegistryFactory {
    public static Registry getRegistry(RegistryConfig registryConfig) {
        RegistryCenterEnum registryCenterEnum = registryConfig.getRegistryCenterEnum();
        String host = registryConfig.getHost();
        int port = registryConfig.getPort();

        if (Objects.requireNonNull(registryCenterEnum) == RegistryCenterEnum.ZOOKEEPER) {
            return new ZookeeperRegistry(host, port);
        } else if (Objects.requireNonNull(registryCenterEnum) == RegistryCenterEnum.REDIS) {
            Runtime.getRuntime().addShutdownHook(new Thread(
                    () -> DRpcBootstrap
                            .getInstance()
                            .getGlobalConfig()
                            .getRegistry()
                            .closeProgramAction(ProviderCache.SERVERS_ADDRESS_MAP)
                    )
            );
            return new RedisRegistry(host, port);
        }
        return null;
    }
}
