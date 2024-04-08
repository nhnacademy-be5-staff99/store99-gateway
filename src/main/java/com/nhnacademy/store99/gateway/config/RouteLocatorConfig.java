package com.nhnacademy.store99.gateway.config;

import com.nhnacademy.store99.gateway.filter.HeaderTransformGatewayFilterFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 라우트 로케이터 설정
 *
 * @author seunggyu-kim
 */
@RequiredArgsConstructor
@Configuration
public class RouteLocatorConfig {
    @Bean
    public RouteLocator store99Route(RouteLocatorBuilder builder,
                                     HeaderTransformGatewayFilterFactory headerTransformFilter) {
        return builder.routes()
                .route("api-bookstore", r -> r.path("/api/bookstore/**")
                        .filters(f -> f.rewritePath("api/bookstore/(?<segment>.*)", "/${segment}")
                                .filter(headerTransformFilter.apply(
                                        new HeaderTransformGatewayFilterFactory.Config("X-USER-TOKEN", "X-USER-ID"))))
                        .uri("lb://STORE99-BOOKSTORE-SERVICE"))
                .route("api-coupon", r -> r.path("/api/coupon/**")
                        .filters(f -> f.rewritePath("api/coupon/(?<segment>.*)", "/${segment}"))
                        .uri("lb://STORE99-COUPON-SERVICE"))
                .route("open-bookstore", r -> r.path("/open/bookstore/**")
                        .filters(f -> f.rewritePath("open/bookstore/(?<segment>.*)", "/${segment}"))
                        .uri("lb://STORE99-BOOKSTORE-SERVICE"))
                .route("open-coupon", r -> r.path("/open/coupon/**")
                        .filters(f -> f.rewritePath("open/coupon/(?<segment>.*)", "/${segment}"))
                        .uri("lb://STORE99-COUPON-SERVICE"))
                .build();
    }
}
