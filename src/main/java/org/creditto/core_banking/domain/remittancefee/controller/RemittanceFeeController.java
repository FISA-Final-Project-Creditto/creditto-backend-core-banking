package org.creditto.core_banking.domain.remittancefee.controller;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.remittancefee.dto.RemittanceFeeReq;
import org.creditto.core_banking.domain.remittancefee.entity.FeeRecord;
import org.creditto.core_banking.domain.remittancefee.service.RemittanceFeeService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/remittance-fee")
@RequiredArgsConstructor
public class RemittanceFeeController {

    private final RemittanceFeeService remittanceFeeService;

    // 테스트를 위한 API
    @PostMapping("/calculate")
    public ResponseEntity<BaseResponse<?>> calculateRemittanceFee(@RequestBody RemittanceFeeReq dto) {
        FeeRecord feeRecord = remittanceFeeService.calculateAndSaveFee(dto);
        return ApiResponseUtil.success(SuccessCode.OK, feeRecord);
    }
}
