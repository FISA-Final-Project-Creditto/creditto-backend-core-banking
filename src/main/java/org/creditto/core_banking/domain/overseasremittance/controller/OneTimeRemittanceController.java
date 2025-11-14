package org.creditto.core_banking.domain.overseasremittance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceRequestDto;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.service.OneTimeRemittanceService;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/remittance")
public class OneTimeRemittanceController {
    private final OneTimeRemittanceService oneTimeRemittanceService;

    // 일회성 송금 컨트롤러
    @PostMapping
    public BaseResponse<OverseasRemittanceResponseDto> processRemittance(@Valid @RequestBody OverseasRemittanceRequestDto request) {
        var result = oneTimeRemittanceService.processRemittance(request);
        return (BaseResponse<OverseasRemittanceResponseDto>) BaseResponse.of(SuccessCode.OK, result);
    }

}