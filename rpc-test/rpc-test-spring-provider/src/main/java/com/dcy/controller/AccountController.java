package com.dcy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kyle
 * @date 2024/03/30
 */
@RestController
@RequestMapping("/account")
public class AccountController {
    @GetMapping
    public String login() {
        return "login successful";
    }
}
