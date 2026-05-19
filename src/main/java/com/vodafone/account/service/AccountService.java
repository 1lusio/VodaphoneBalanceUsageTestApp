package com.vodafone.account.service;

import com.vodafone.account.client.UsageApiClient;
import com.vodafone.account.dto.AccountSummaryResponse;
import com.vodafone.account.dto.UsageCounterDto;
import com.vodafone.account.dto.UsageItemDto;
import com.vodafone.account.exception.CustomerNotFoundException;
import com.vodafone.account.model.UserBalance;
import com.vodafone.account.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserBalanceRepository balanceRepository;
    private final UsageApiClient usageApiClient;

    public AccountSummaryResponse getAccountSummary(Long customerId) {
        UserBalance userBalance = balanceRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        List<UsageItemDto> usageItems = usageApiClient.fetchUsage(customerId);

        Map<String, UsageCounterDto> usage = usageItems.stream()
                .collect(Collectors.toMap(
                        UsageItemDto::type,
                        item -> new UsageCounterDto(item.currentSpent(), item.total())
                ));

        return new AccountSummaryResponse(userBalance.getId(), userBalance.getBalance(), usage);
    }
}
