package com.dcy.rpc;

import com.dcy.rpc.bootstrap.DRpcBootstrap;
import com.dcy.rpc.enumeration.CompressTypeEnum;
import com.dcy.rpc.enumeration.LoadbalancerTypeEnum;
import com.dcy.rpc.enumeration.RegistryCenterEnum;
import com.dcy.rpc.enumeration.SerializeTypeEnum;
import com.dcy.rpc.service.BookService;
import com.dcy.rpc.service.impl.BookServiceImpl;

/**
 * @author Kyle
 * @date 2024/02/19
 */
public class Consumer {
    public static void main(String[] args) {
        DRpcBootstrap.getInstance()
                .setBootstrapName("RPC-consumer")
                .registry(RegistryCenterEnum.ZOOKEEPER, "192.168.200.128", 2181)
                .serialize(SerializeTypeEnum.JDK)
                .compress(CompressTypeEnum.DEFLATE)
                .loadbalancer(LoadbalancerTypeEnum.ROUND_ROBIN);
                //.reference("com.dcy.rpc.service.impl");

        BookService bookService = new BookServiceImpl();
        bookService.writeReaderName();
        bookService.writeReaderName();
        bookService.writeReaderName();
        bookService.writeReaderName();
    }
}