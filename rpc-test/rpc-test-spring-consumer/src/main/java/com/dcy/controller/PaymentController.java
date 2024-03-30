package com.dcy.controller;

import com.dcy.rpc.UserService;
import com.dcy.rpc.proxy.ProxyConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kyle
 * @date 2024/03/30
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final UserService userService = new ProxyConfig<>(UserService.class).get();

    @GetMapping
    public String payment() {
        return userService.sayHello("Kyle");
    }
}
