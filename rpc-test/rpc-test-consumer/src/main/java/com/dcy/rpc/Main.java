package com.dcy.rpc;

import com.dcy.rpc.bootstrap.DRpcBootstrap;

/**
 * @author Kyle
 * @date 2024/02/19
 */
public class Main {
    public static void main(String[] args) {
        DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-consumer")
                .registry(null)
                .serialize()
                .compress()
                .reference();
    }
}