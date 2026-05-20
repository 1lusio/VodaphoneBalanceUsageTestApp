package com.vodafone.account.service;

import com.vodafone.account.client.UsageApiClient;
import com.vodafone.account.dto.AccountSummaryResponse;
import com.vodafone.account.dto.UsageItemDto;
import com.vodafone.account.exception.CustomerNotFoundException;
import com.vodafone.account.model.UserBalance;
import com.vodafone.account.repository.UserBalanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserBalanceRepository balanceRepository;

    @Mock
    private UsageApiClient usageApiClient;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("aggregates balance and usage into a single response")
    void getAccountSummary_returnsAggregatedResponse() {
        UserBalance balance = new UserBalance(1L, new BigDecimal("250.75"));
        when(balanceRepository.findById(1L)).thenReturn(Optional.of(balance));
        when(usageApiClient.fetchUsage(1L)).thenReturn(List.of(
                new UsageItemDto("min", 120, 500),
                new UsageItemDto("sms", 50, 100),
                new UsageItemDto("internet", 2048, 5120)
        ));

        AccountSummaryResponse response = accountService.getAccountSummary(1L);

        assertThat(response.id()).isEqualTo("1");
        assertThat(response.balance()).isEqualByComparingTo("250.75");
        assertThat(response.usage()).hasSize(3);
        assertThat(response.usage().get("min").currentSpent()).isEqualTo(120);
        assertThat(response.usage().get("min").total()).isEqualTo(500);
        assertThat(response.usage().get("sms").currentSpent()).isEqualTo(50);
        assertThat(response.usage().get("internet").currentSpent()).isEqualTo(2048);
        assertThat(response.usage().get("internet").total()).isEqualTo(5120);
    }

    @Test
    @DisplayName("throws CustomerNotFoundException when customer is not in the database")
    void getAccountSummary_throwsCustomerNotFoundWhenIdMissing() {
        when(balanceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountSummary(99L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("returns empty usage map when external API returns no items")
    void getAccountSummary_handlesEmptyUsageList() {
        UserBalance balance = new UserBalance(1L, new BigDecimal("250.75"));
        when(balanceRepository.findById(1L)).thenReturn(Optional.of(balance));
        when(usageApiClient.fetchUsage(1L)).thenReturn(List.of());

        AccountSummaryResponse response = accountService.getAccountSummary(1L);

        assertThat(response.id()).isEqualTo("1");
        assertThat(response.usage()).isEmpty();
    }

    @Test
    @DisplayName("propagates UsageServiceException when external API call fails")
    void getAccountSummary_propagatesUsageServiceException() {
        UserBalance balance = new UserBalance(1L, new BigDecimal("100.00"));
        when(balanceRepository.findById(1L)).thenReturn(Optional.of(balance));
        when(usageApiClient.fetchUsage(1L))
                .thenThrow(new com.vodafone.account.exception.UsageServiceException(
                        "connection refused", new RuntimeException()));

        assertThatThrownBy(() -> accountService.getAccountSummary(1L))
                .isInstanceOf(com.vodafone.account.exception.UsageServiceException.class);
    }
}
