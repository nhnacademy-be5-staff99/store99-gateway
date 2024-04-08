package com.nhnacademy.store99.gateway.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

/**
 * 헤더 변환 게이트웨이 필터 팩토리
 * <p>요청 헤더를 변환하는 게이트웨이 필터 팩토리. 인증 개발 전 임시로 사용
 *
 * @author seunggyu-kim
 */
@Component
public class HeaderTransformGatewayFilterFactory extends
        AbstractGatewayFilterFactory<HeaderTransformGatewayFilterFactory.Config> {

    public HeaderTransformGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String headerValue = request.getHeaders().getFirst(config.getHeaderName());

            if (headerValue != null) {
                request = request.mutate().header(config.getNewHeaderName(), headerValue).build();
            }

            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    @Getter
    @RequiredArgsConstructor
    public static class Config {
        private final String headerName;
        private final String newHeaderName;
    }
}