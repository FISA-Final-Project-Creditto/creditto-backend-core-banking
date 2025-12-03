package org.creditto.core_banking.domain.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.creditscore.service.CreditScoreService;
import org.creditto.core_banking.domain.exchange.dto.*;
import org.creditto.core_banking.domain.exchange.service.ExchangeService;
import org.creditto.core_banking.global.response.ApiResponseUtil;
import org.creditto.core_banking.global.response.BaseResponse;
import org.creditto.core_banking.global.response.SuccessCode;
import org.springframework.http.ResponseEntity;
import org.creditto.core_banking.global.common.CurrencyCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final CreditScoreService  creditScoreService;

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
     * 특정 통화 최신 환율 정보 조회
     *
     * @param currency 조회할 통화
     * @return 성공 응답 및 특정 통화 최신 환율 정보
     */
    @GetMapping("/{currency}")
    public ResponseEntity<BaseResponse<SingleExchangeRateRes>> getExchangeRate(@PathVariable String currency) {
        CurrencyCode currencyCode = CurrencyCode.from(currency);
        return ApiResponseUtil.success(SuccessCode.OK, exchangeService.getRateByCurrency(currencyCode));
    }

    @GetMapping("/preferential-rate/{userId}")
    public ResponseEntity<BaseResponse<PreferentialRateRes>> getPreferentialRate(@PathVariable Long userId) {
        double rate = creditScoreService.getPreferentialRate(userId);
        return ApiResponseUtil.success(SuccessCode.OK, new  PreferentialRateRes(rate));
    }
}
