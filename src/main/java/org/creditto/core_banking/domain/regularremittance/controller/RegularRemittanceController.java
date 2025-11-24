package org.creditto.core_banking.domain.regularremittance.controller;

import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceCreateReqDto;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceResponseDto;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceUpdateReqDto;
import org.creditto.core_banking.domain.regularremittance.dto.RemittanceHistoryResDto;
import org.creditto.core_banking.domain.regularremittance.service.RegularRemittanceService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
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

    // Task 1: 사용자 정기송금 설정 내역 조회
    @GetMapping("/schedule")
    public List<RegularRemittanceResponseDto> getScheduledRemittancesByUserId(@RequestParam("userId") String userId) {
        return regularRemittanceService.getScheduledRemittancesByUserId(userId);
    }

    // Task 2: 하나의 정기송금 설정에 대한 송금 기록 조회
    @GetMapping("/schedule/{regRemId}")
    public List<RemittanceHistoryResDto> getRemittanceRecordsByRecurId(@PathVariable("regRemId") Long regRemId, @RequestParam("userId") String userId) {
        return regularRemittanceService.getRegularRemittanceHistoryByRegRemId(userId, regRemId);
    }

    // Task 3: 단일 송금 내역 상세 조회
    @GetMapping("/{remittanceId}/detail")
    public ResponseEntity<?> getRegularRemittanceDetail(
            @PathVariable Long remittanceId,
            @RequestParam("userId") String userId
    ) {
        return ApiResponseUtil.success(SuccessCode.OK, regularRemittanceService.getRegularRemittanceDetail(userId, remittanceId));
    }

    // 정기 해외 송금 설정 신규 등록
    @PostMapping("/schedule")
    public ResponseEntity<BaseResponse<RegularRemittanceResponseDto>> createScheduledRemittance(
            @RequestParam("userId") String userId,
            @RequestBody RegularRemittanceCreateReqDto dto
    ) {
//        regularRemittanceService.createScheduledRemittance(userId, dto);
//        return ApiResponseUtil.success(SuccessCode.OK);
        RegularRemittanceResponseDto createdRemittance = regularRemittanceService.createScheduledRemittance(userId, dto);
        return ApiResponseUtil.success(SuccessCode.CREATED, createdRemittance);
    }

    // 등록된 정기 해외 송금 설정 수정
    @PutMapping("/schedule/{recurId}")
    public ResponseEntity<BaseResponse<Void>> updateScheduledRemittance(
            @PathVariable("recurId") Long recurId,
            @RequestParam("userId") String userId,
            @RequestBody RegularRemittanceUpdateReqDto dto
    ) {
        regularRemittanceService.updateScheduledRemittance(recurId, userId, dto);
        return ApiResponseUtil.success(SuccessCode.OK);
    }

    // 정기 해외 송금 설정 삭제
    @DeleteMapping("/schedule/{recurId}")
    public ResponseEntity<BaseResponse<Void>> deleteScheduledRemittance(
            @PathVariable("recurId") Long recurId,
            @RequestParam("userId") String userId
    ) {
        regularRemittanceService.deleteScheduledRemittance(recurId, userId);
        return ApiResponseUtil.success(SuccessCode.OK);
    }

}
