package com.dcy.rpc.service.impl;

import com.dcy.rpc.UserService;
import com.dcy.rpc.annotation.RpcService;

/**
 * @author Kyle
 * @date 2024/02/26
 */
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public String sayHello(String stuName) {
        return "hello " + stuName + ". How are you?";
    }
}