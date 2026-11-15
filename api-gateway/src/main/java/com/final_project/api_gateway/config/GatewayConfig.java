package com.final_project.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${app.api-key:final-project-api-key-2026}")
    private String apiKey;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-route", r -> r
                        .path("/auth/**")
                        .filters(f -> f.addRequestHeader("X-API-KEY", apiKey))
                        .uri("lb://auth-service"))
                .route("product-route", r -> r
                        .path("/product/**")
                        .filters(f -> f.addRequestHeader("X-API-KEY", apiKey))
                        .uri("lb://product-service"))
                .build();
    }
}
