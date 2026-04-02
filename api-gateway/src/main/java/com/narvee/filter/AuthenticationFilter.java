package com.narvee.filter;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.narvee.client.TenantClient;
import com.narvee.util.JwtTokenUtil;

import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private TenantClient tenantClient;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();

            // ---- Public endpoints bypass ----
            if (!validator.isSecured.test(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                jwtUtil.validateToken(token);

                String role = jwtUtil.getRole(token);
                String tenantId = jwtUtil.getTenant(token);

                // ---- Reactive Tenant Validation ----
                if (tenantId != null) {
                    return tenantClient.isTenantActive(tenantId)
                            .flatMap(isActive -> {
                                if (!isActive) {
                                    return onError(exchange, "Company account is deactivated", HttpStatus.FORBIDDEN);
                                }

                                // Proper request mutation
                                ServerWebExchange mutatedExchange = exchange.mutate()
                                        .request(exchange.getRequest().mutate()
                                                .header("X-User-Role", role)
                                                .header("X-Tenant-Id", tenantId)
                                                .build())
                                        .build();

                                return chain.filter(mutatedExchange);
                            });
                }

                return chain.filter(exchange);

            } catch (ExpiredJwtException e) {
                return onError(exchange, "Token Expired", HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

//    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
//        exchange.getResponse().setStatusCode(status);
//        byte[] bytes = err.getBytes(StandardCharsets.UTF_8);
//        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
//        return exchange.getResponse().writeWith(Mono.just(buffer));
//    }
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");

        String body = "{\"error\": \"" + err + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        DataBuffer buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    public static class Config {}
}