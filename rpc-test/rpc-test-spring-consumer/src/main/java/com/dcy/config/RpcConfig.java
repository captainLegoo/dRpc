package com.dcy.config;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.enumeration.CompressTypeEnum;
import com.dcy.rpc.enumeration.LoadbalancerTypeEnum;
import com.dcy.rpc.enumeration.RegistryCenterEnum;
import com.dcy.rpc.enumeration.SerializeTypeEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kyle
 * @date 2024/03/30
 * <p>
 * rpc-consumer config
 */
@Configuration
public class RpcConfig {

    @Bean
    public DRpcBootstrap rpcConsumerConfig() {
        DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-consumer")
                .registry(RegistryCenterEnum.ZOOKEEPER, "192.168.64.128", 2181)
                .serialize(SerializeTypeEnum.JDK)
                .compress(CompressTypeEnum.DEFLATE)
                .loadbalancer(LoadbalancerTypeEnum.ROUND_ROBIN);

        return DRpcBootstrap.getInstance();
    }
}
