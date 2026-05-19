package com.vodafone.account.client;

import com.vodafone.account.dto.UsageItemDto;
import com.vodafone.account.exception.UsageServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UsageApiClientTest {

    @Autowired
    private UsageApiClient usageApiClient;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    @DisplayName("parses all usage types from the external API response")
    void fetchUsage_parsesResponseCorrectly() {
        mockServer.expect(requestTo("http://localhost:9090/usage/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [
                          {"type":"min","currentSpent":120,"total":500},
                          {"type":"sms","currentSpent":50,"total":100},
                          {"type":"internet","currentSpent":2048,"total":5120}
                        ]
                        """, MediaType.APPLICATION_JSON));

        List<UsageItemDto> result = usageApiClient.fetchUsage(1L);

        mockServer.verify();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).type()).isEqualTo("min");
        assertThat(result.get(0).currentSpent()).isEqualTo(120);
        assertThat(result.get(0).total()).isEqualTo(500);
    }

    @Test
    @DisplayName("wraps HTTP errors in UsageServiceException")
    void fetchUsage_throwsUsageServiceExceptionOnServerError() {
        mockServer.expect(requestTo("http://localhost:9090/usage/1"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> usageApiClient.fetchUsage(1L))
                .isInstanceOf(UsageServiceException.class);

        mockServer.verify();
    }

    @Test
    @DisplayName("ignores unknown fields in the external API response")
    void fetchUsage_ignoresUnknownFields() {
        mockServer.expect(requestTo("http://localhost:9090/usage/1"))
                .andRespond(withSuccess("""
                        [{"type":"min","currentSpent":100,"total":500,"extraField":"ignored"}]
                        """, MediaType.APPLICATION_JSON));

        List<UsageItemDto> result = usageApiClient.fetchUsage(1L);

        mockServer.verify();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("returns empty list when external API responds with an empty array")
    void fetchUsage_handlesEmptyResponse() {
        mockServer.expect(requestTo("http://localhost:9090/usage/1"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        List<UsageItemDto> result = usageApiClient.fetchUsage(1L);

        mockServer.verify();
        assertThat(result).isEmpty();
    }
}
