package com.github.liyibo1110.secondkill.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.liyibo1110.secondkill.common.constant.CommonConstants;
import com.github.liyibo1110.secondkill.common.exception.ErrorEnum;
import com.github.liyibo1110.secondkill.common.jwt.JwtUtils;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import com.github.liyibo1110.secondkill.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关鉴权过滤器。
 * 从请求头中提取JWT的token，验证通过后将userId写入X-User-Id头，传递给下游服务。
 * 白名单内的路径（例如活动列表页），不需要登录也可以访问。
 *
 * 关键安全措施：下游服务通过X-User-Id头来获取当前用户身份，但这个头可以被客户端伪造，所以在这个过滤器会先移除请求中已有的头。
 * @author liyibo
 * @date 2026-06-23 11:17
 */
@Slf4j
@Component
public class AuthGatewayFilter implements GlobalFilter, Ordered {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 不用登录也可以访问的路径：
     * 1、活动列表。
     * 2、商品详情。
     */
    private static final List<String> WHITE_LIST = List.of(
            "/api/seckill/activity/**",
            "/api/seckill/product/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // url匹配了白名单，则不进行用户验证，直接通过
        if (isWhiteListed(path))
            return chain.filter(exchange);

        String authorization = request.getHeaders().getFirst(CommonConstants.HEADER_AUTHORIZATION);
        // 没有头，或者不合规，直接算不通过
        if (authorization == null || !authorization.startsWith(CommonConstants.TOKEN_PREFIX))
            return unauthorized(exchange);

        // 解析token，获取userId
        String token = authorization.substring(CommonConstants.TOKEN_PREFIX.length());
        Long userId;
        try {
            userId = JwtUtils.getUserId(token);
        } catch (Exception e) {
            StructuredLog.warn(log)
                    .message("token验证失败")
                    .put("path", path)
                    .exception(e)
                    .log();
            return unauthorized(exchange);
        }

        // 移除客户端可能伪造的X-User-Id，再注入token解析出来的userId
        ServerHttpRequest mutatedRequest = request.mutate()
                .headers(headers -> headers.remove(CommonConstants.HEADER_USER_ID))
                .header(CommonConstants.HEADER_USER_ID, String.valueOf(userId))
                .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 在TraceGatewayFilter之后，在Sentinel之前。
     */
    @Override
    public int getOrder() {
        return -50;
    }

    private boolean isWhiteListed(String path) {
        for (String pattern : WHITE_LIST) {
            if (PATH_MATCHER.match(pattern, path))
                return true;
        }
        return false;
    }

    /**
     * 验证失败时，直接返回401和统一的JSON响应体，不把请求继续转发给下游服务了。
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Result<?> result = Result.fail(ErrorEnum.UNAUTHORIZED);
        // 以下代码时WebFlux中写response的标准方式，需要把数据封装成DataBuffer
        try {
            byte[] bytes = MAPPER.writeValueAsString(result).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }
}
