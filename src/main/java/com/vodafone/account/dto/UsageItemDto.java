package com.vodafone.account.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UsageItemDto(String type, int currentSpent, int total) {}
