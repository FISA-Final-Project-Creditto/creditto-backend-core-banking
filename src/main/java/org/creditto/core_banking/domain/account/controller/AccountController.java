package org.creditto.core_banking.domain.account.controller;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.dto.AccountCreateReq;
import org.creditto.core_banking.domain.account.dto.AccountRes;
import org.creditto.core_banking.domain.account.service.AccountService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/{externalUserId}")
    public ResponseEntity<BaseResponse<?>> createAccount(@RequestBody AccountCreateReq request,
                                                         @PathVariable String externalUserId) {
        return ApiResponseUtil.success(SuccessCode.CREATED, accountService.createAccount(request, externalUserId));
    }

    @GetMapping("/{accountId}/account")
    public ResponseEntity<BaseResponse<?>> getAccountByAccountId(@PathVariable Long accountId) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountById(accountId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BaseResponse<?>> getAccountBalanceByAccountId(@PathVariable Long accountId) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountBalanceById(accountId));
    }

    @GetMapping("/{accountNo}")
    public ResponseEntity<BaseResponse<?>> getAccountByAccountNo(@PathVariable String accountNo) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountByAccountNo(accountNo));
    }

    @GetMapping("client/{externalUserId}")
    public ResponseEntity<BaseResponse<?>> getAccountByClientId(@PathVariable String externalUserId) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountByExternalId(externalUserId));
    }
}
