package com.nhnacademy.store99.gateway.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "store99.route")
public class RouteLocatorProperties {
    String bookstorePath;
    String bookstoreUri;
    String couponPath;
    String couponUri;
    String tokenBookstorePath;
    String tokenBookstoreUri;
    String tokenCouponPath;
    String tokenCouponUri;
}
