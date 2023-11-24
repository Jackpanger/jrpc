package com.jackpang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description: ApplicationConsumer
 * date: 11/24/23 6:07 AM
 * author: jinhao_pang
 * version: 1.0
 */
@SpringBootApplication
@RestController
public class ApplicationConsumer {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationConsumer.class, args);
    }

    @GetMapping("/test")
    public  String hello() {
        return "hello consumer";
    }
}
