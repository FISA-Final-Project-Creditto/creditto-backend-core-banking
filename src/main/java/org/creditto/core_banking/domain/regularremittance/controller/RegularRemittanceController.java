package org.creditto.core_banking.domain.regularremittance.controller;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.regularremittance.service.RegularRemittanceService;
import org.creditto.core_banking.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/remittance/schedule")
public class RegularRemittanceController {

    private final RegularRemittanceService regularRemittanceService;

    // 해외 정기 송금 처리
    @PostMapping
    public ResponseEntity<BaseResponse<?>> process(@PathVariable Long accountId) {
        return null;
    }

    // 해외 정기 송금 내역
    @GetMapping
    public ResponseEntity<BaseResponse<?>> getList(@PathVariable Long regRemId) {
        return null;
    }


}
