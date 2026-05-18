package com.vodafone.account.client;

import com.vodafone.account.dto.UsageItemDto;
import com.vodafone.account.exception.UsageServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class UsageApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UsageApiClient(RestTemplate restTemplate,
                          @Value("${usage.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<UsageItemDto> fetchUsage(Long customerId) {
        String url = baseUrl + "/usage/" + customerId;
        log.debug("Fetching usage data from {}", url);
        try {
            List<UsageItemDto> body = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UsageItemDto>>() {}
            ).getBody();
            return body != null ? body : List.of();
        } catch (RestClientException ex) {
            log.warn("Usage API call failed for customer id={}: {}", customerId, ex.getMessage());
            throw new UsageServiceException(
                    "Failed to retrieve usage data for customer " + customerId, ex);
        }
    }
}
