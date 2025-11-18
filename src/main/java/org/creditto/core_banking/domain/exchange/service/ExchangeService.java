package org.creditto.core_banking.domain.exchange.service;

import jakarta.transaction.Transactional;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRateProvider exchangeRateProvider;
    private final ExchangeRepository exchangeRepository;

    private static final BigDecimal SPREAD_RATE = new BigDecimal("0.01");
    private static final BigDecimal PREFERENTIAL_RATE = new BigDecimal("0.5");
    public static final CurrencyCode KRW_CURRENCY_CODE = CurrencyCode.KRW;
    private static final int ADJUSTED_RATE_SCALE = 4;

    /**
     * 최신 환율 정보를 조회하여 반환합니다.
     * @return 통화 코드를 키로 하는 환율 정보 맵
     */
    public Map<String, ExchangeRateRes> getLatestRates() {
        return exchangeRateProvider.getExchangeRates();
    }

    /**
     * 환전 요청을 처리하는 메인 메서드입니다.
     * @param request 환전 요청 정보 (from, to, 금액)
     * @return 환전 처리 결과
     */
    @Transactional
    public ExchangeRes exchange(ExchangeReq request) {
        // 환전 전 통화와 환전 후 통화가 같은지 검증
        if (request.fromCurrency().equals(request.toCurrency())) {
            throw new CustomBaseException(ErrorBaseCode.SAME_CURRENCY_EXCHANGE_NOT_ALLOWED);
        }

        Map<String, ExchangeRateRes> rateMap = exchangeRateProvider.getExchangeRates();
        boolean isKrwToForeign = KRW_CURRENCY_CODE.equals(request.fromCurrency());

        if (isKrwToForeign || KRW_CURRENCY_CODE.equals(request.toCurrency())) {
            return performExchange(request, rateMap, isKrwToForeign);
        } else {
            // 원화가 포함되지 않은 환전은 지원하지 않음
            throw new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
        }
    }

    /**
     * 실제 환전 계산 로직을 수행하는 내부 메서드입니다.
     * @param request 환전 요청 정보
     * @param rateMap 조회된 전체 환율 맵
     * @param isKrwToForeign 원화에서 외화로의 환전 여부 (true: 원화->외화, false: 외화->원화)
     * @return 환전 처리 결과
     */
    private ExchangeRes performExchange(ExchangeReq request, Map<String, ExchangeRateRes> rateMap, boolean isKrwToForeign) {
        // 1. 외화 통화 결정
        CurrencyCode foreignCurrency = isKrwToForeign ? request.toCurrency() : request.fromCurrency();

        // 2. 환율 정보 조회 및 계산
        BigDecimal baseRateFromApi = getBaseRateForCurrency(rateMap, foreignCurrency);
        BigDecimal adjustedBaseRate = baseRateFromApi.divide(new BigDecimal(foreignCurrency.getUnit()), ADJUSTED_RATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal appliedRate = calculateAppliedRate(adjustedBaseRate, isKrwToForeign);

        // 3. 송금액(fromAmount) 및 수취액(toAmount) 계산
        BigDecimal fromAmount;
        BigDecimal toAmount;
        if (isKrwToForeign) { // 원화 -> 외화
            toAmount = request.targetAmount(); // 외화 수취액
            fromAmount = toAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING); // 원화 송금액
        } else { // 외화 -> 원화
            fromAmount = request.targetAmount(); // 외화 송금액
            toAmount = fromAmount.multiply(appliedRate).setScale(0, RoundingMode.FLOOR); // 원화 수취액
        }

        // 4. 환전 내역 저장
        Exchange savedExchange = saveExchangeHistory(request, fromAmount, toAmount, baseRateFromApi);

        // 5. USD 환율 정보 조회
        BigDecimal exchangeRateUSD = getBaseRateForCurrency(rateMap, CurrencyCode.USD);

        // 6. 최종 결과 반환
        return new ExchangeRes(
            savedExchange.getId(),
            request.fromCurrency(),
            request.toCurrency(),
            appliedRate.setScale(2, RoundingMode.HALF_UP),
            fromAmount,
            exchangeRateUSD
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
     * 환율 맵에서 특정 통화에 대한 환율 정보를 찾습니다.
     * @param rateMap 전체 환율 맵
     * @param currency 조회할 통화
     * @return 해당 통화의 환율 정보 DTO
     */
    private ExchangeRateRes findRate(Map<String, ExchangeRateRes> rateMap, CurrencyCode currency) {
        return Optional.ofNullable(rateMap.get(currency.getLookupKey()))
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED));
    }

    /**
     * 환전 내역을 데이터베이스에 저장합니다.
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
}