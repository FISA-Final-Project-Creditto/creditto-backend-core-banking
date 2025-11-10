package org.creditto.core_banking.domain.overseasremittance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceRequestDto;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.service.OverseasRemittanceService;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/remittance")
public class OverseasRemittanceController {
    private final OverseasRemittanceService overseasRemittanceService;


    @GetMapping    // 해외송금 내역 조회
    public BaseResponse<?> getRemittanceList(@RequestParam String clientId) {
        List<OverseasRemittanceResponseDto> result = overseasRemittanceService.getRemittanceList(clientId);
        return BaseResponse.of(SuccessCode.OK, result);
    }

    @PostMapping   // 해외송금 처리_일회성 (계좌 잔액 차감 + 송금 이력 저장)
    public BaseResponse<?> processRemittance(@Valid @RequestBody OverseasRemittanceRequestDto request) {
        var result = overseasRemittanceService.processRemittance(request);
        return BaseResponse.of(SuccessCode.OK, result);
    }

}
