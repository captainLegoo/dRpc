package com.dcy.rpc;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.enumeration.RegistryCenterEnum;

/**
 * @author Kyle
 * @date 2024/02/19
 */
public class Provider {
    public static void main(String[] args) {
        DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-Provider")
                .port(9000)
                .registry(RegistryCenterEnum.ZOOKEEPER, "192.168.200.128", 2181)
                .scanAndPublish("com.dcy.rpc.service.impl")
                .start();
    }
}