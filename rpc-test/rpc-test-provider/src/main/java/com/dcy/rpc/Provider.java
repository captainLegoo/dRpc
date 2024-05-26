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
                .port(9001)
                //.registry(RegistryCenterEnum.ZOOKEEPER, "192.168.205.132", 2181)
                .registry(RegistryCenterEnum.REDIS, "192.168.205.128", 6379)
                .scanAndPublish("com.dcy.rpc.service.impl")
                .start();
    }
}