package com.github.liyibo1110.secondkill.processor;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 服务启动入口。
 * @author liyibo
 * @date 2026-06-25 10:36
 */
@SpringBootApplication(scanBasePackages = {
        "com.github.liyibo1110.secondkill.processor",
        "com.github.liyibo1110.secondkill.common"
})
@EnableDiscoveryClient
@EnableDubbo
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
