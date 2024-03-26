package com.dcy.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kyle
 * @date 2024/03/26
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @RequestMapping
    public String hello(){
        return "Payment Successful";
    }
}
