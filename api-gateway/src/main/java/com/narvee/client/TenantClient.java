
package com.narvee.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TenantClient {

    private final WebClient webClient;

    public TenantClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://platform-service").build();
    }
    public Mono<Boolean> isTenantActive(String tenantId) {
        return webClient.get()
        		.uri("/platform/tenant/internal/{tenantId}/is-active", tenantId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false); // if service fails, treat as inactive
    }
}