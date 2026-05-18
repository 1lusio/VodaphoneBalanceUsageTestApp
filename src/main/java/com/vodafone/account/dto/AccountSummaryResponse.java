package com.vodafone.account.dto;

import java.math.BigDecimal;
import java.util.Map;

public record AccountSummaryResponse(
        Long id,
        BigDecimal balance,
        Map<String, UsageCounterDto> usage
) {}
