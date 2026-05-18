package com.vodafone.account.controller;

import com.vodafone.account.dto.AccountSummaryResponse;
import com.vodafone.account.service.AccountService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{id}/account-summary")
    public ResponseEntity<AccountSummaryResponse> getAccountSummary(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(accountService.getAccountSummary(id));
    }
}
