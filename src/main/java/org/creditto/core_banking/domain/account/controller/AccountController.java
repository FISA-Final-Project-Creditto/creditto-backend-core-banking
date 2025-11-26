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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/{userId}")
    public ResponseEntity<BaseResponse<AccountRes>> createAccount(@RequestBody AccountCreateReq request,
                                                                  @PathVariable Long userId) {
        return ApiResponseUtil.success(SuccessCode.CREATED, accountService.createAccount(request, userId));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BaseResponse<AccountRes>> getAccountByAccountId(@PathVariable Long accountId) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountById(accountId));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BaseResponse<BigDecimal>> getAccountBalanceByAccountId(@PathVariable Long accountId) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountBalanceById(accountId));
    }

    @GetMapping(params = "accountNo")
    public ResponseEntity<BaseResponse<AccountRes>> getAccountByAccountNo(@RequestParam(name = "accountNo") String accountNo) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountByAccountNo(accountNo));
    }

    @GetMapping(params = "userId")
    public ResponseEntity<BaseResponse<List<AccountRes>>> getAccountByClientId(@RequestParam(name = "userId") Long userId) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountByUserId(userId));
    }
}
