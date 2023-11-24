package com.jackpang;

import com.jackpang.annotation.JrpcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * description: HelloController
 * date: 11/24/23 6:23 AM
 * author: jinhao_pang
 * version: 1.0
 */
@RestController
public class HelloController {
    // a proxy object will be injected
    @JrpcService
    private HelloJrpc helloJrpc;
    @GetMapping("/hello")
    public String hello() {
        return helloJrpc.sayHi("Hi, provider!");
    }
}
