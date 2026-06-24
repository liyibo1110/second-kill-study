package com.github.liyibo1110.secondkill.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.common.exception.ErrorEnum;
import com.github.liyibo1110.secondkill.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sentinel网关限流配置，也是gateway服务的最后一个环节。
 *
 * 限流规则会配置在Nacos Config里，支持运行时动态调整。
 * 启动时会先先加载默认规则来兜底，Nacos推送新规则后将其覆盖。
 * @author liyibo
 * @date 2026-06-23 11:53
 */
@Slf4j
@Configuration
public class SentinelConfig {

    /**
     * Nacos Config中的限流规则JSON。
     * 格式为：[{"resource":"second-kill-service","count":1000,"intervalSec":1}]
     */
    @Value("${sentinel.gateway.rules:}")
    private String gatewayRulesJson;

    @PostConstruct
    public void init() {
        initBlockHandler();
        loadGatewayRules();
    }

    /**
     * Sentinel默认的限流响应，是一个HTML错误页面，并不适用于我们常用的API接口，因此需要自定义一个BlockRequestHandler。
     * 触发限流时会返回HTTP 429和统一的JSON结构，前端可以根据结构展示相应提示，再次说明4xx属于业务异常，不会触发Sentinel的熔断统计。
     */
    private void initBlockHandler() {
        GatewayCallbackManager.setBlockHandler((exchange, throwable) ->
                ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                              .contentType(MediaType.APPLICATION_JSON)
                              .body(BodyInserters.fromValue(Result.fail(ErrorEnum.TOO_MANY_REQUESTS))));
    }

    /**
     * 加载网关限流规则，优先从Nacos Config中加载，配置为空或者解析失败时，使用默认兜底规则。
     */
    public void loadGatewayRules() {
        Set<GatewayFlowRule> rules = parseRulesFromConfig();
        if (rules.isEmpty()) {
            rules = defaultGatewayRules();
            log.info("使用默认限流规则，规则数量: {}", rules.size());
        } else {
            log.info("从配置中心加载限流规则，规则数量: {}", rules.size());
        }
    }

    /**
     * 解析Nacos Config中的限流规则JSON。
     */
    private Set<GatewayFlowRule> parseRulesFromConfig() {
        if (gatewayRulesJson == null || gatewayRulesJson.isBlank())
            return Set.of();

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> ruleList = mapper.readValue(gatewayRulesJson, new TypeReference<>() {});
            Set<GatewayFlowRule> rules = new HashSet<>();

            for (Map<String, Object> item : ruleList) {
                String resource = (String) item.get("resource");
                Number count = (Number) item.get("count");
                Number intervalSec = (Number) item.getOrDefault("intervalSec", 1);
                if (resource != null && count != null) {
                    rules.add(new GatewayFlowRule(resource)
                            .setCount(count.doubleValue())
                            .setIntervalSec(intervalSec.intValue()));
                }
            }
            return rules;
        } catch (Exception e) {
            log.warn("限流规则配置解析失败，将使用默认规则: {}", e.getMessage());
            return Set.of();
        }
    }

    /**
     * 默认写死的限流规则，用来兜底。
     */
    private Set<GatewayFlowRule> defaultGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 秒杀服务：承接C端秒杀请求，QPS上限1000
        rules.add(new GatewayFlowRule("second-kill-service")
                .setCount(1000)
                .setIntervalSec(1));

        // 管理后台：运营人员使用，QPS上限200
        rules.add(new GatewayFlowRule("second-kill-admin")
                .setCount(200)
                .setIntervalSec(1));

        // 支撑服务：内部调用为主，QPS上限500
        rules.add(new GatewayFlowRule("second-kill-support")
                .setCount(500)
                .setIntervalSec(1));

        return rules;
    }
}
