package org.creditto.core_banking.domain.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
import org.creditto.core_banking.domain.exchange.service.ExchangeService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/core/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    /**
     * 최신 환율 정보 조회
     *
     * @return 성공 응답 및 최신 환율 정보 리스트
     */
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, ExchangeRateRes>>> getExchangeRates() {
        return ApiResponseUtil.success(SuccessCode.OK, exchangeService.getLatestRates());
    }

    /**
     * 환전 요청 처리
     *
     * @param request 환전 요청 정보 (계좌 ID, 통화, 금액 등)
     * @return 성공 응답 및 환전 결과 정보
     */
    @PostMapping
    public ResponseEntity<BaseResponse<ExchangeRes>> exchange(@RequestBody ExchangeReq request) {
        return ApiResponseUtil.success(SuccessCode.OK, exchangeService.exchange(request));
    }
}
