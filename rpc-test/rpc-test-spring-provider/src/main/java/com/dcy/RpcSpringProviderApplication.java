package com.dcy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class RpcSpringProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcSpringProviderApplication.class, args);
    }
}
