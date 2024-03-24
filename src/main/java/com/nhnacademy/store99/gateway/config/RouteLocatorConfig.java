package com.nhnacademy.store99.gateway.config;

import com.nhnacademy.store99.gateway.property.RouteLocatorProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class RouteLocatorConfig {
    private final RouteLocatorProperties routeLocatorProperties;

    @Bean
    public RouteLocator store99Route(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("bookstore", r -> r.path(routeLocatorProperties.getBookstorePath())
                        .uri(routeLocatorProperties.getBookstoreUri()))
                .route("coupon", r -> r.path(routeLocatorProperties.getCouponPath())
                        .uri(routeLocatorProperties.getCouponUri()))
                .route("token-bookstore", r -> r.path(routeLocatorProperties.getTokenBookstorePath())
                        .uri(routeLocatorProperties.getTokenBookstoreUri()))
                .route("token-coupon", r -> r.path(routeLocatorProperties.getTokenCouponPath())
                        .uri(routeLocatorProperties.getTokenCouponUri()))
                .build();
    }
}
