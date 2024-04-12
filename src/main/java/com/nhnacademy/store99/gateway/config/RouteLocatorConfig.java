package com.nhnacademy.store99.gateway.config;

import com.nhnacademy.store99.gateway.filter.AuthorizationFilter;
import com.nhnacademy.store99.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 라우트 로케이터 설정
 *
 * @author seunggyu-kim
 * @author Ahyeon Song
 */
@RequiredArgsConstructor
@Configuration
public class RouteLocatorConfig {

    @Bean
    public RouteLocator store99Route(RouteLocatorBuilder builder,
                                     RedisTemplate<String, Object> redisTemplate,
                                     JwtUtil jwtUtil,
                                     AuthorizationFilter authorizationFilter
    ) {
        return builder.routes()
                .route("api-bookstore", r -> r.path("/api/bookstore/**")
                        .filters(f -> f.rewritePath("api/bookstore/(?<segment>.*)", "/${segment}")
                                .filter(tokenFilter(authorizationFilter, redisTemplate, jwtUtil)))
                        .uri("lb://STORE99-BOOKSTORE-SERVICE"))
                .route("api-coupon", r -> r.path("/api/coupon/**")
                        .filters(f -> f.rewritePath("api/coupon/(?<segment>.*)", "/${segment}")
                                .filter(tokenFilter(authorizationFilter, redisTemplate, jwtUtil)))
                        .uri("lb://STORE99-COUPON-SERVICE"))
                .route("open-bookstore", r -> r.path("/open/bookstore/**")
                        .filters(f -> f.rewritePath("open/bookstore/(?<segment>.*)", "/${segment}"))
                        .uri("lb://STORE99-BOOKSTORE-SERVICE"))
                .route("open-coupon", r -> r.path("/open/coupon/**")
                        .filters(f -> f.rewritePath("open/coupon/(?<segment>.*)", "/${segment}"))
                        .uri("lb://STORE99-COUPON-SERVICE"))
                .route("auth", r -> r.path("/auth/**")
                        .filters(f -> f.rewritePath("auth/(?<segment>.*)", "/${segment}"))
                        .uri("lb://STORE99-AUTH-SERVICE"))
                .build();
    }

    /**
     * authorizationFilter 추가
     *
     * @param authorizationFilter
     * @param redisTemplate
     * @param jwtUtil
     * @return GatewayFilter
     */
    private GatewayFilter tokenFilter(AuthorizationFilter authorizationFilter,
                                      RedisTemplate<String, Object> redisTemplate,
                                      JwtUtil jwtUtil) {
        return authorizationFilter.apply(
                new AuthorizationFilter.Config(redisTemplate, jwtUtil));
    }
}
