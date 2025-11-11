package org.creditto.core_banking.domain.account.controller;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.service.AccountService;
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
@RequestMapping("/api/core/account")
public class AccountController {

    private final AccountService accountService;


    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BaseResponse<?>> getBalance(@PathVariable Long accountId) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountById(accountId));
    }

    @GetMapping("/{accountNo}")
    public ResponseEntity<BaseResponse<?>> getBalance(@PathVariable String accountNo) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountByAccountNo(accountNo));
    }

    @GetMapping("client/{clientId}")
    public ResponseEntity<BaseResponse<?>> findByClientId(@PathVariable String clientId) {
        return ApiResponseUtil.success(SuccessCode.OK, accountService.getAccountByClientId(clientId));
    }
}
