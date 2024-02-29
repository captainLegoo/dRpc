package com.dcy.rpc.service.impl;

import com.dcy.rpc.UserService;
import com.dcy.rpc.annotation.RpcReference;
import com.dcy.rpc.proxy.ProxyConfig;
import com.dcy.rpc.service.BookService;

/**
 * @author Kyle
 * @date 2024/02/27
 */
public class BookServiceImpl implements BookService {

    @RpcReference
    private UserService userService = new ProxyConfig<>(UserService.class).get();

    @Override
    public void writeReaderName() {
        String string = userService.sayHello("dcy");
        System.out.println("string = " + string);
    }
}
