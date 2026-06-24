package com.github.liyibo1110.secondkill.base.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注册Jackson时间模块。
 * @author liyibo
 * @date 2026-06-23 15:21
 */
@Configuration
@ConditionalOnClass(ObjectMapper.class)
public class SecondKillJacksonConfig {

    @Bean
    public SecondKillJavaTimeModule secondKillJavaTimeModule() {
        return new SecondKillJavaTimeModule();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer seckillJacksonCustomizer(SecondKillJavaTimeModule timeModule) {
        return builder -> builder.modules(timeModule);
    }
}
