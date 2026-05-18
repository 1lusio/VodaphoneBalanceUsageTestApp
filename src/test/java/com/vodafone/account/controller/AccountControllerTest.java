package com.vodafone.account.controller;

import com.vodafone.account.dto.AccountSummaryResponse;
import com.vodafone.account.dto.UsageCounterDto;
import com.vodafone.account.exception.CustomerNotFoundException;
import com.vodafone.account.exception.UsageServiceException;
import com.vodafone.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    @DisplayName("returns 200 with aggregated balance and usage")
    void getAccountSummary_returns200WithAggregatedBody() throws Exception {
        AccountSummaryResponse response = new AccountSummaryResponse(
                1L,
                new BigDecimal("250.75"),
                Map.of(
                        "min", new UsageCounterDto(120, 500),
                        "sms", new UsageCounterDto(50, 100),
                        "internet", new UsageCounterDto(2048, 5120)
                )
        );
        when(accountService.getAccountSummary(1L)).thenReturn(response);

        mockMvc.perform(get("/customers/1/account-summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.balance").value(250.75))
                .andExpect(jsonPath("$.usage.min.currentSpent").value(120))
                .andExpect(jsonPath("$.usage.min.total").value(500))
                .andExpect(jsonPath("$.usage.sms.currentSpent").value(50))
                .andExpect(jsonPath("$.usage.internet.currentSpent").value(2048))
                .andExpect(jsonPath("$.usage.internet.total").value(5120));
    }

    @Test
    @DisplayName("returns 404 with detail message when customer not found")
    void getAccountSummary_returns404WhenCustomerNotFound() throws Exception {
        when(accountService.getAccountSummary(99L))
                .thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/customers/99/account-summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Customer not found with id: 99"));
    }

    @Test
    @DisplayName("returns 502 when external usage API is unreachable")
    void getAccountSummary_returns502WhenUsageServiceFails() throws Exception {
        when(accountService.getAccountSummary(1L))
                .thenThrow(new UsageServiceException("connection refused", new RuntimeException()));

        mockMvc.perform(get("/customers/1/account-summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("returns 400 when id is not a number")
    void getAccountSummary_returns400ForInvalidIdType() throws Exception {
        mockMvc.perform(get("/customers/abc/account-summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("returns 400 when id is zero or negative")
    void getAccountSummary_returns400ForNonPositiveId() throws Exception {
        mockMvc.perform(get("/customers/0/account-summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
