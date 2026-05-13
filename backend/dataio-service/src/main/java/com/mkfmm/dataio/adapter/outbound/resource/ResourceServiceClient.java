package com.mkfmm.dataio.adapter.outbound.resource;

import com.mkfmm.dataio.application.port.outbound.ResourceServicePort;
import com.mkfmm.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class ResourceServiceClient implements ResourceServicePort {

    private final WebClient webClient;

    public ResourceServiceClient(@Value("${mkfmm.resource-service.url}") String baseUrl,
                                 @Value("${mkfmm.resource-service.timeout:30s}") Duration timeout) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public boolean checkDuplicate(String key, String resourceType) {
        Map<String, Object> response = webClient.post()
                .uri("/internal/resources/check-duplicate")
                .bodyValue(Map.of("key", key, "resourceType", resourceType))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(30));
        return response != null && Boolean.TRUE.equals(response.get("duplicate"));
    }

    @Override
    public String findExistingId(String key, String resourceType) {
        Map<String, Object> response = webClient.post()
                .uri("/internal/resources/check-duplicate")
                .bodyValue(Map.of("key", key, "resourceType", resourceType))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(30));
        if (response == null || response.get("existingId") == null) {
            throw new BusinessException("DATAIO_006", "리소스 ID를 찾을 수 없습니다");
        }
        return String.valueOf(response.get("existingId"));
    }

    @Override
    public void createResource(Map<String, String> resource, String userId) {
        webClient.post()
                .uri("/internal/resources")
                .header("X-User-Id", userId)
                .bodyValue(resource)
                .retrieve()
                .bodyToMono(Void.class)
                .block(Duration.ofSeconds(30));
    }

    @Override
    public void updateResource(String id, Map<String, String> resource, String userId) {
        webClient.put()
                .uri("/internal/resources/{id}", id)
                .header("X-User-Id", userId)
                .bodyValue(resource)
                .retrieve()
                .bodyToMono(Void.class)
                .block(Duration.ofSeconds(30));
    }

    @Override
    public List<Map<String, Object>> searchResources(Map<String, String> params) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/internal/resources/search");
                    params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block(Duration.ofSeconds(30));
    }

    @Override
    public List<Map<String, Object>> findAllActive() {
        return webClient.get()
                .uri("/internal/resources/all")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block(Duration.ofSeconds(30));
    }
}
