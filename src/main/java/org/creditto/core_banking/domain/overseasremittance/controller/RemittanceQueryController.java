package org.creditto.core_banking.domain.overseasremittance.controller;
// 송금에 관하여 일회/정기 구분 없이 통합해서

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.service.RemittanceQueryService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 송금 내역 조회를 처리하는 API 컨트롤러입니다.
 * 일회성 및 정기 송금을 포함한 모든 송금 내역을 조회할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/core/remittance")
public class RemittanceQueryController {

    private final RemittanceQueryService remittanceService;

    /**
     * 특정 고객(Client)의 모든 송금 내역을 조회합니다.
     *
     * @param userId 송금 내역을 조회할 고객의 ID
     * @return 해당 고객의 송금 내역 리스트 ({@link OverseasRemittanceResponseDto})
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<OverseasRemittanceResponseDto>>> getRemittanceList(@RequestParam Long userId) {
        return ApiResponseUtil.success(SuccessCode.OK, remittanceService.getRemittanceList(userId));
    }
}
