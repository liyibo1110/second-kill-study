package com.github.liyibo1110.secondkill.gateway.filter;

import com.github.liyibo1110.secondkill.common.constant.CommonConstants;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 网关链路追踪器。
 *
 * 给每个请求，生成或透传traceId，写入请求头传递给下游服务。
 * 同时写入响应头，供客户端关联日志。
 *
 * Spring Cloud Gateway基于WebFlux，所以不能依赖MDC做线程级透传，因为WebFlux的请求处理在Netty的EventLoop线程上，
 * 一个请求的不同阶段，可能会在不同的线程上执行，MDC这种基于ThreadLocal的机制在这里不可靠。
 * 所以网关层只负责在HTTP头部传递traceId，下游service服务通过TraceFilter（在common模块）从请求头中读取。
 * 这样旧不会依赖任何线程上下文机制，在WebFlux和Servlet两种模型之间可以无缝衔接。
 * @author liyibo
 * @date 2026-06-23 10:50
 */
@Component
public class TraceGatewayFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 尝试生成traceId
        String traceId = request.getHeaders().getFirst(CommonConstants.HEADER_TRACE_ID);
        if (traceId == null || traceId.isBlank())
            traceId = generateTraceId();

        /**
         * 将traceId写入请求头，下游服务的TraceFilter会从这个头里取。
         * 这里使用了mutate方法，因为在WebFlux中ServerHttpRequest是不可变对象，不能直接修改请求头，
         * 必须创建一个新的Builder，修改后生成新的Request对象，再放到Exchange里面，这属于WebFlux的基础操作。
         */
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(CommonConstants.HEADER_TRACE_ID, traceId)
                .build();

        // 将traceId也写入响应头，客户端可以用它关联请求和日志
        exchange.getResponse().getHeaders().add(CommonConstants.HEADER_TRACE_ID, traceId);
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 优先级最高，在鉴权和限流之前执行。
     */
    @Override
    public int getOrder() {
        return -100;
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
