package com.github.liyibo1110.secondkill.base;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务启动入口。
 * @author liyibo
 * @date 2026-06-23 13:40
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
