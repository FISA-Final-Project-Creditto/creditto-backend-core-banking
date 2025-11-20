package org.creditto.core_banking.domain.regularremittance.controller;

import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceResponseDto;
import org.creditto.core_banking.domain.regularremittance.service.RegularRemittanceService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/remittance")
public class RegularRemittanceController {

    private final RegularRemittanceService regularRemittanceService;

    public RegularRemittanceController(RegularRemittanceService regularRemittanceService) {
        this.regularRemittanceService = regularRemittanceService;
    }

    // 등록된 정기 해외 송금 설정 조회
    @GetMapping("/schedule")
    public List<RegularRemittanceResponseDto> getScheduledRemittancesByUserId(@RequestParam("userId") String userId) {
        return regularRemittanceService.getScheduledRemittancesByUserId(userId);
    }

    // 한 건의 정기 해외 송금 설정 상세 조회
    // 내역 조회인데 여기 있어도 되는지 모르겠음
    @GetMapping("/schedule/{recurId}")
    public List<OverseasRemittanceResponseDto> getRemittanceRecordsByRecurId(@PathVariable("recurId") Long recurId, @RequestParam("userId") String userId) {
        return regularRemittanceService.getRemittanceRecordsByRecurId(recurId);
    }

    // 등록된 정기 해외 송금 설정 수정
    @PutMapping("/schedule/{recurId}")
    public ResponseEntity<BaseResponse<?>> updateScheduledRemittance(
            @PathVariable("recurId") Long recurId,
            @RequestParam("userId") String userId,
            @RequestBody org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceUpdateReqDto dto
    ) {
        regularRemittanceService.updateScheduledRemittance(recurId, userId, dto);
        return ApiResponseUtil.success(org.creditto.core_banking.global.response.SuccessCode.OK);
    }
}
