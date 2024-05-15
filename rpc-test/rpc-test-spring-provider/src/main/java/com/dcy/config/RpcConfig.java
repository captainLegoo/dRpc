package com.dcy.config;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.enumeration.RegistryCenterEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kyle
 * @date 2024/03/30
 * <p>
 * rpc-provider config
 */
@Configuration
public class RpcConfig {

    @Bean
    public DRpcBootstrap rpcProviderConfig() {
        DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-Provider")
                .port(9600)
                .registry(RegistryCenterEnum.ZOOKEEPER, "192.168.205.132", 2181)
                .scanAndPublish("com.dcy.service.impl")
                .start();
        return DRpcBootstrap.getInstance();
    }
}
