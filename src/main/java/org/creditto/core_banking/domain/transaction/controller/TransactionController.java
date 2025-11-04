package org.creditto.core_banking.domain.transaction.controller;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.transaction.service.TransactionService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{accountId}")
    public ResponseEntity<BaseResponse<?>> getTransactions(@PathVariable Long accountId) {
        return ApiResponseUtil.success(SuccessCode.OK, transactionService.findByAccountId(accountId));
    }
}
