package com.dcy.service.impl;

import com.dcy.rpc.UserService;
import com.dcy.rpc.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * @author Kyle
 * @date 2024/03/30
 */
@Service
@RpcService
public class UserServiceImpl implements UserService {
    @Override
    public String sayHello(String name) {
        return "Rpc-Provider say Hello to: " + name;
    }
}
