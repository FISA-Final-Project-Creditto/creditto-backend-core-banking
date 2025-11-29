package org.creditto.core_banking.domain.exchange.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
import org.creditto.core_banking.domain.exchange.entity.Exchange;
import org.creditto.core_banking.domain.exchange.repository.ExchangeRepository;
import org.creditto.core_banking.global.common.CurrencyCode;
import org.creditto.core_banking.global.feign.ExchangeRateProvider;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExchangeService {

    private final ExchangeRateProvider exchangeRateProvider;
    private final ExchangeRepository exchangeRepository;

    private static final BigDecimal SPREAD_RATE = new BigDecimal("0.01");
    private static final BigDecimal PREFERENTIAL_RATE = new BigDecimal("0.5");
    public static final CurrencyCode KRW_CURRENCY_CODE = CurrencyCode.KRW;
    private static final int ADJUSTED_RATE_SCALE = 4;
    private static final int USD_CALCULATION_SCALE = 10; // 새로운 상수 추가

    /**
     * 최신 환율 정보를 조회하여 반환
     * @return 통화 코드를 키로 하는 환율 정보 맵
     */
    public Map<String, ExchangeRateRes> getLatestRates() {
        return exchangeRateProvider.getExchangeRates();
    }

    /**
     * 환전 요청을 처리하는 메인 메서드
     * @param request 환전 요청 정보 (from, to, 금액)
     * @return 환전 처리 결과
     */
    @Transactional
    public ExchangeRes exchange(ExchangeReq request) {
        // 환전 전 통화와 환전 후 통화가 같은지 검증
        if (request.fromCurrency().equals(request.toCurrency())) {
            throw new CustomBaseException(ErrorBaseCode.SAME_CURRENCY_EXCHANGE_NOT_ALLOWED);
        }

        boolean isKrwToForeign = KRW_CURRENCY_CODE.equals(request.fromCurrency());

        if (isKrwToForeign || KRW_CURRENCY_CODE.equals(request.toCurrency())) {
            Map<String, ExchangeRateRes> rateMap = exchangeRateProvider.getExchangeRates(); // API 호출을 필요한 시점으로 이동
            return doExchange(request, rateMap, isKrwToForeign);
        } else {
            // 원화가 포함되지 않은 환전은 지원하지 않음
            throw new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
        }
    }

    /**
     * 실제 환전 계산 로직을 수행하는 내부 메서드
     * '받을 금액(toAmount)'을 기준으로 계산을 수행
     * @param request 환전 요청 정보
     * @param rateMap 조회된 전체 환율 맵
     * @param isKrwToForeign 원화에서 외화로의 환전 여부 (true: 원화->외화, false: 외화->원화)
     * @return 환전 처리 결과
     */
    private ExchangeRes doExchange(ExchangeReq request, Map<String, ExchangeRateRes> rateMap, boolean isKrwToForeign) {
        // 외화 통화 결정
        CurrencyCode foreignCurrency = isKrwToForeign ? request.toCurrency() : request.fromCurrency();

        // USD 환율 정보 미리 조회 (중복 호출 방지)
        BigDecimal exchangeRateUSD = getBaseRateForCurrency(rateMap, CurrencyCode.USD);

        // 환율 정보 조회 및 계산
        BigDecimal baseRateFromApi;
        if (foreignCurrency == CurrencyCode.USD) {
            baseRateFromApi = exchangeRateUSD; // USD인 경우 미리 조회한 값 재사용
        } else {
            baseRateFromApi = getBaseRateForCurrency(rateMap, foreignCurrency);
        }

        BigDecimal adjustedBaseRate = baseRateFromApi.divide(new BigDecimal(foreignCurrency.getUnit()), ADJUSTED_RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal appliedRate = calculateAppliedRate(adjustedBaseRate, isKrwToForeign);

        // 받을 금액 기준으로 보낼 금액 계산
        BigDecimal fromAmount;
        BigDecimal toAmount = request.targetAmount();

        if (isKrwToForeign) { // 원화 -> 외화
            // 받을 외화를 위해 내야 할 원화 계산
            fromAmount = toAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING);
        } else { // 외화 -> 원화
            // 받을 원화를 위해 내야 할 외화 계산
            fromAmount = toAmount.divide(appliedRate, 2, RoundingMode.CEILING);
        }

        // 환전 내역 저장
        Exchange savedExchange = saveExchangeHistory(request, fromAmount, toAmount, baseRateFromApi);

        // fromAmount의 USD 가치 계산
        BigDecimal fromAmountInUSD;
        BigDecimal fromCurrencyBaseRate;
        if (request.fromCurrency() == CurrencyCode.KRW) {
            fromCurrencyBaseRate = BigDecimal.ONE;
        } else {
            fromCurrencyBaseRate = getBaseRateForCurrency(rateMap, request.fromCurrency());
        }
        fromAmountInUSD = getSendAmountInUSD(fromAmount, fromCurrencyBaseRate, request.fromCurrency(), exchangeRateUSD);


        // 최종 결과 반환
        return new ExchangeRes(
            savedExchange.getId(),
            request.fromCurrency(),
            request.toCurrency(),
            baseRateFromApi,
            fromAmount,
            fromAmountInUSD
        );
    }

    /**
     * 적용 환율을 계산하는 메서드
     * @param baseRate 매매 기준율
     * @param isBuying 외화 매수 여부 (원화->외화: true, 외화->원화: false)
     * @return 최종 적용 환율
     */
    private BigDecimal calculateAppliedRate(BigDecimal baseRate, boolean isBuying) {
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        if (isBuying) {
            // 살 때: 매매기준율 * (1 + 유효 스프레드)
            return baseRate.multiply(BigDecimal.ONE.add(effectiveSpread));
        } else {
            // 팔 때: 매매기준율 * (1 - 유효 스프레드)
            return baseRate.multiply(BigDecimal.ONE.subtract(effectiveSpread));
        }
    }

    /**
     * 환율 맵에서 특정 통화에 대한 환율 정보 찾음
     * @param rateMap 전체 환율 맵
     * @param currency 조회할 통화
     * @return 해당 통화의 환율 정보 DTO
     */
    private ExchangeRateRes findRate(Map<String, ExchangeRateRes> rateMap, CurrencyCode currency) {
        return Optional.ofNullable(rateMap.get(currency.getLookupKey()))
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED));
    }

    /**
     * 환전 내역을 데이터베이스에 저장
     * @param exchangeReq 원본 환전 요청
     * @param fromAmount 계산된 송금액
     * @param toAmount 계산된 수취액
     * @param rate 적용된 API 원본 환율
     * @return 저장된 Exchange 엔티티
     */
    private Exchange saveExchangeHistory(ExchangeReq exchangeReq, BigDecimal fromAmount, BigDecimal toAmount, BigDecimal rate) {
        Exchange exchange = Exchange.of(
                exchangeReq,
                fromAmount,
                toAmount,
                rate
        );
        return exchangeRepository.save(exchange);
    }

    /**
     * 환율 정보 DTO에서 매매 기준율을 BigDecimal 타입으로 추출
     * @param rateMap 전체 환율 맵
     * @param currency 조회할 통화
     * @return 정제된 매매 기준율
     */
    private BigDecimal getBaseRateForCurrency(Map<String, ExchangeRateRes> rateMap, CurrencyCode currency) {
        ExchangeRateRes rateInfo = findRate(rateMap, currency);
        return new BigDecimal(rateInfo.getBaseRate().replace(",", ""));
    }

    /**
     * 특정 금액을 USD 가치로 변환
     * @param sendAmount 변환할 금액
     * @param exchangeRate 해당 통화의 기준 환율 (원화 대비)
     * @param currency 변환할 통화 코드
     * @param exchangeRateUSD USD의 기준 환율 (원화 대비)
     * @return USD로 변환된 금액 (소수점 둘째 자리까지 반올림)
     */
    private BigDecimal getSendAmountInUSD(BigDecimal sendAmount, BigDecimal exchangeRate, CurrencyCode currency, BigDecimal exchangeRateUSD) {
        if (exchangeRateUSD == null || exchangeRateUSD.compareTo(BigDecimal.ZERO) == 0) {
            throw new CustomBaseException(ErrorBaseCode.EXCHANGE_RATE_EXPIRED);
        }

        RoundingMode rounding = RoundingMode.HALF_UP;

        // JPY, IDR 같은 단위가 있는 통화 처리
        BigDecimal baseRate;
        if (currency.getUnit() > 1) {
            baseRate = exchangeRate.divide(BigDecimal.valueOf(currency.getUnit()), USD_CALCULATION_SCALE, rounding);
        } else {
            baseRate = exchangeRate;
        }

        // (통화 기준 환율 / USD 기준 환율) = 통화 1단위당 USD 가치
        BigDecimal calculatedRate = baseRate.divide(exchangeRateUSD, USD_CALCULATION_SCALE, rounding);

        BigDecimal sendAmountInUSD = sendAmount.multiply(calculatedRate);

        return sendAmountInUSD.setScale(2, RoundingMode.HALF_UP);
    }
}