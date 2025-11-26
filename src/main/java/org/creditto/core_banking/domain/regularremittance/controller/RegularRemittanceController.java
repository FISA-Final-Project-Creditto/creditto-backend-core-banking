package org.creditto.core_banking.domain.regularremittance.controller;

import org.creditto.core_banking.domain.regularremittance.dto.*;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceResponseDto;
import org.creditto.core_banking.domain.regularremittance.service.RegularRemittanceService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/core/remittance/schedule")
public class RegularRemittanceController {

    private final RegularRemittanceService regularRemittanceService;

    public RegularRemittanceController(RegularRemittanceService regularRemittanceService) {
        this.regularRemittanceService = regularRemittanceService;
    }

    /**
     * 특정 사용자의 모든 정기송금 설정 내역을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 모든 정기송금 설정 목록 ({@link RegularRemittanceResponseDto})
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<RegularRemittanceResponseDto>>> getScheduledRemittancesByUserId(@RequestParam("userId") Long userId) {
        return ApiResponseUtil.success(SuccessCode.OK, regularRemittanceService.getScheduledRemittancesByUserId(userId));
    }

    /**
     * 특정 정기송금 설정에 대한 모든 송금 기록을 조회합니다.
     *
     * @param regRemId 정기송금 ID
     * @param userId 사용자 ID
     * @return 해당 정기송금 설정에 대한 모든 송금 기록 목록 ({@link RemittanceHistoryDto})
     */
    @GetMapping("/{regRemId}")
    public ResponseEntity<BaseResponse<List<RemittanceHistoryDto>>> getRemittanceRecordsByRecurId(@PathVariable("regRemId") Long regRemId, @RequestParam("userId") Long userId) {
        return ApiResponseUtil.success(SuccessCode.OK, regularRemittanceService.getRegularRemittanceHistoryByRegRemId(userId, regRemId));
    }

    /**
     * 단일 정기송금 내역의 상세 정보를 조회합니다.
     *
     * @param regRemId 정기송금 ID
     * @param remittanceId 송금 ID
     * @param userId 사용자 ID
     * @return 해당 송금의 상세 정보 ({@link RemittanceDetailDto})
     */
    @GetMapping("/{regRemId}/{remittanceId}")
    public ResponseEntity<BaseResponse<RemittanceDetailDto>> getRegularRemittanceDetail(
            @PathVariable Long regRemId,
            @PathVariable Long remittanceId,
            @RequestParam("userId") Long userId
    ) {
        return ApiResponseUtil.success(SuccessCode.OK, regularRemittanceService.getRegularRemittanceDetail(userId, remittanceId, regRemId));
    }

    /**
     * 신규 정기송금을 등록합니다.
     *
     * @param userId 사용자 ID
     * @param dto 정기송금 생성에 필요한 정보를 담은 DTO
     * @return 생성된 정기송금 정보 ({@link RegularRemittanceResponseDto})
     */
    @PostMapping("/add")
    public ResponseEntity<BaseResponse<RegularRemittanceResponseDto>> createScheduledRemittance(
            @RequestParam("userId") Long userId,
            @RequestBody RegularRemittanceCreateDto dto
    ) {
        RegularRemittanceResponseDto createdRemittance = regularRemittanceService.createScheduledRemittance(userId, dto);
        return ApiResponseUtil.success(SuccessCode.CREATED, createdRemittance);
    }

    /**
     * 기존 정기 해외송금 설정을 수정합니다.
     *
     * @param regRemId 정기송금 ID
     * @param userId 사용자 ID
     * @param dto 정기송금 수정에 필요한 정보를 담은 DTO
     * @return 성공 응답 (HTTP 200 OK)
     */
    @PutMapping("/{regRemId}")
    public ResponseEntity<BaseResponse<Void>> updateScheduledRemittance(
            @PathVariable("regRemId") Long regRemId,
            @RequestParam("userId") Long userId,
            @RequestBody RegularRemittanceUpdateDto dto
    ) {
        regularRemittanceService.updateScheduledRemittance(regRemId, userId, dto);
        return ApiResponseUtil.success(SuccessCode.OK);
    }

    /**
     * 기존 정기 해외송금 설정을 삭제합니다.
     *
     * @param regRemId 정기송금 ID
     * @param userId 사용자 ID
     * @return 성공 응답 (HTTP 200 OK)
     */
    @DeleteMapping("/{regRemId}")
    public ResponseEntity<BaseResponse<Void>> deleteScheduledRemittance(
            @PathVariable("regRemId") Long regRemId,
            @RequestParam("userId") Long userId
    ) {
        regularRemittanceService.deleteScheduledRemittance(regRemId, userId);
        return ApiResponseUtil.success(SuccessCode.OK);
    }

}
