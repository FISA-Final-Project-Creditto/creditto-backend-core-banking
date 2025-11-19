package org.creditto.core_banking.domain.overseasremittance.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceRequestDto;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.service.OneTimeRemittanceService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 일회성 해외송금 요청을 처리하는 API 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/remittance")
public class OneTimeRemittanceController {
    private final OneTimeRemittanceService oneTimeRemittanceService;

    /**
     * 일회성 해외송금 요청을 받아 처리합니다.
     *
     * @param request 송금에 필요한 정보(고객 ID, 수취인 정보, 금액 등)를 담은 DTO
     * @return 처리 결과를 담은 응답 DTO ({@link OverseasRemittanceResponseDto})
     */
    @PostMapping
    public ResponseEntity<?> processRemittance(@Valid @RequestBody OverseasRemittanceRequestDto request) {
        return ApiResponseUtil.success(SuccessCode.OK, oneTimeRemittanceService.processRemittance(request));
    }
}