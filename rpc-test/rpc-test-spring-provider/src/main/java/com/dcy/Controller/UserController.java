package com.dcy.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kyle
 * @date 2024/03/26
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @GetMapping
    public String user() {
        return "user";
    }
}
