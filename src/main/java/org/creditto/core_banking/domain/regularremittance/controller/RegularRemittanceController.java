package org.creditto.core_banking.domain.regularremittance.controller;

import org.creditto.core_banking.domain.regularremittance.dto.*;
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
    public List<RegularRemittanceResponseDto> getScheduledRemittancesByUserId(@RequestParam("userId") Long userId) {
        return regularRemittanceService.getScheduledRemittancesByUserId(userId);
    }

    // Task 2: 하나의 정기송금 설정에 대한 송금 기록 조회
    @GetMapping("/schedule/{regRemId}")
    public List<RemittanceHistoryDto> getRemittanceRecordsByRecurId(@PathVariable("regRemId") Long regRemId, @RequestParam("userId") Long userId) {
        return regularRemittanceService.getRegularRemittanceHistoryByRegRemId(userId, regRemId);
    }

    // Task 3: 단일 송금 내역 상세 조회
    @GetMapping("/{remittanceId}/detail")
    public ResponseEntity<BaseResponse<RemittanceDetailDto>> getRegularRemittanceDetail(
            @PathVariable Long remittanceId,
            @RequestParam("userId") Long userId
    ) {
        return ApiResponseUtil.success(SuccessCode.OK, regularRemittanceService.getRegularRemittanceDetail(userId, remittanceId));
    }

    // Task 4: 정기송금 신규 등록
    @PostMapping("/schedule/add")
    public ResponseEntity<BaseResponse<RegularRemittanceResponseDto>> createScheduledRemittance(
            @RequestParam("userId") Long userId,
            @RequestBody RegularRemittanceCreateDto dto
    ) {
//        regularRemittanceService.createScheduledRemittance(userId, dto);
//        return ApiResponseUtil.success(SuccessCode.OK);
        RegularRemittanceResponseDto createdRemittance = regularRemittanceService.createScheduledRemittance(userId, dto);
        return ApiResponseUtil.success(SuccessCode.CREATED, createdRemittance);
    }

    // Task 5: 정기 해외 송금 설정 수정
    @PutMapping("/schedule/{regRemId}")
    public ResponseEntity<BaseResponse<Void>> updateScheduledRemittance(
            @PathVariable("regRemId") Long regRemId,
            @RequestParam("userId") Long userId,
            @RequestBody RegularRemittanceUpdateDto dto
    ) {
        regularRemittanceService.updateScheduledRemittance(regRemId, userId, dto);
        return ApiResponseUtil.success(SuccessCode.OK);
    }

    // 정기 해외 송금 설정 삭제
    @DeleteMapping("/schedule/{regRemId}")
    public ResponseEntity<BaseResponse<Void>> deleteScheduledRemittance(
            @PathVariable("regRemId") Long regRemId,
            @RequestParam("userId") Long userId
    ) {
        regularRemittanceService.deleteScheduledRemittance(regRemId, userId);
        return ApiResponseUtil.success(SuccessCode.OK);
    }

}
