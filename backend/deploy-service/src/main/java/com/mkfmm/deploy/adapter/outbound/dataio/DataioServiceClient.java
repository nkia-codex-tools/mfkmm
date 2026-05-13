package com.mkfmm.deploy.adapter.outbound.dataio;

import com.mkfmm.deploy.application.port.outbound.DataioServicePort;
import com.mkfmm.shared.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
public class DataioServiceClient implements DataioServicePort {

    private final WebClient webClient;

    public DataioServiceClient(@Value("${mkfmm.dataio-service.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public byte[] exportAll(String format, String userId) {
        try {
            return webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/dataio/export/all")
                            .queryParam("format", format)
                            .build())
                    .header("X-User-Id", userId)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block(Duration.ofSeconds(60));
        } catch (Exception e) {
            throw new BusinessException("DEPLOY_002", "DataIO 서비스 호출 실패: " + e.getMessage());
        }
    }
}
