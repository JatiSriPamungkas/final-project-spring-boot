package com.final_project.api_gateway.filter;

import com.final_project.api_gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Cek apakah path diawali /auth/** -> jika ya, bypass (lanjut tanpa cek token)
        if (path.startsWith("/auth")) {
            return chain.filter(exchange);
        }

        // 2. Jika path diawali /product/**
        if (path.startsWith("/product")) {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Jika header tidak ada atau tidak diawali Bearer -> reject 401
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Header Authorization tidak ada atau tidak diawali 'Bearer '");
            }

            String token = authHeader.substring(7);

            // Parse & validasi signature JWT dengan secret key. Jika invalid/expired -> reject 401
            if (!jwtUtil.validateToken(token)) {
                return onError(exchange, HttpStatus.UNAUTHORIZED, "Token JWT tidak valid atau sudah kedaluwarsa");
            }
        }

        // 3. Jika valid -> teruskan request
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = String.format("{\"status\":%d,\"message\":\"%s\",\"data\":null}", httpStatus.value(), message);
        DataBuffer buffer = response.bufferFactory().wrap(jsonBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
