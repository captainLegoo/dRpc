package com.dcy.controller;

import com.dcy.rpc.UserService;
import com.dcy.rpc.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kyle
 * @date 2024/03/30
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    //private final UserService userService = new ProxyConfig<>(UserService.class).get();
    @RpcReference
    private UserService userService;

    @GetMapping("/{accountName}")
    public String payment(@PathVariable String accountName) {
        return userService.sayHello(accountName);
    }
}
