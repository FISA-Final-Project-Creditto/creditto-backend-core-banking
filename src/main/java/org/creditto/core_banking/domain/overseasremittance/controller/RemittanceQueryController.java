package org.creditto.core_banking.domain.overseasremittance.controller;
// 송금에 관하여 일회/정기 구분 없이 통합해서

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.service.RemittanceQueryService;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/remittance")
public class RemittanceQueryController {

    private final RemittanceQueryService remittanceService;

    // 조회 컨트롤러
    @GetMapping
    public BaseResponse<List<OverseasRemittanceResponseDto>> getRemittanceList(@RequestParam String clientId) {
        List<OverseasRemittanceResponseDto> result = remittanceService.getRemittanceList(clientId);
        return (BaseResponse<List<OverseasRemittanceResponseDto>>) BaseResponse.of(SuccessCode.OK, result);
    }
}
