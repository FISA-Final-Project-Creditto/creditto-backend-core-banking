package org.creditto.core_banking.domain.exchange.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRateProvider exchangeRateProvider;
    private final ExchangeRepository exchangeRepository;

    // 환전 시 적용될 스프레드 비율 (1%)
    private static final BigDecimal SPREAD_RATE = new BigDecimal("0.01");

    // 환전 시 적용될 환율 우대 비율 (50%)
    // TODO: 추후 사용자별 우대 환율 업데이트
    private static final BigDecimal PREFERENTIAL_RATE = new BigDecimal("0.5");

    // 원화 통화 코드 상수
    public static final CurrencyCode KRW_CURRENCY_CODE = CurrencyCode.KRW;

    /**
     * 외부 API를 통해 최신 환율 정보를 조회합니다.
     *
     * @return 최신 환율 정보 리스트
     */
    public List<ExchangeRateRes> getLatestRates() {
        return exchangeRateProvider.getExchangeRates();
    }

    /**
     * 환전 요청을 처리하는 메인 메서드
     * 환전 방향에 따라 적절한 private 메서드를 호출하여 처리를 위임
     *
     * @param request 환전 요청 정보 (출발/도착 통화, 대상 금액 등)
     * @return 환전 결과 정보 (출발/도착 통화, 적용 환율, 환전 금액)
     */
    @Transactional
    public ExchangeRes exchange(ExchangeReq request) {
        // 최신 환율 정보 조회
        List<ExchangeRateRes> rates = exchangeRateProvider.getExchangeRates();

        // 원화 -> 외화 환전 로직
        if (KRW_CURRENCY_CODE.equals(request.fromCurrency())) {
            return handleKrwToForeignExchange(request,rates);
        }
        // 외화 -> 원화 환전 로직
        else if (KRW_CURRENCY_CODE.equals(request.toCurrency())) {
            return handleForeignToKrwExchange(request, rates);
        }
        // 지원하지 않는 거래 예외 처리
        else {
            throw new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
        }
    }

    /**
     * 원화에서 외화로의 환전 처리
     */
    private ExchangeRes handleKrwToForeignExchange(ExchangeReq request,List<ExchangeRateRes> rates) {
        // 대상 통화 코드
        CurrencyCode toCurrency = request.toCurrency();

        // 대상 외화의 매매 기준율 정보 조회
        ExchangeRateRes rateInfo = findRate(rates, toCurrency);
        BigDecimal baseRateFromApi = new BigDecimal(rateInfo.getBaseRate().replace(",", ""));

        // 통화 단위(unit)를 고려하여 기준 환율 조정 (예: JPY(100) -> 1 JPY에 대한 환율)
        BigDecimal adjustedBaseRate = baseRateFromApi.divide(new BigDecimal(toCurrency.getUnit()), 4, RoundingMode.HALF_UP);

        // 살 때 환율 계산: 조정된 매매기준율 * (1 + (스프레드 * (1 - 우대율)))
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = adjustedBaseRate.multiply(BigDecimal.ONE.add(effectiveSpread));

        // 출금될 원화 금액 계산
        BigDecimal toAmount = request.targetAmount();
        BigDecimal fromAmount = toAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING);

        // USD 환율 정보 조회
        ExchangeRateRes usdRateInfo = findRate(rates, CurrencyCode.USD);
        BigDecimal exchangeRateUSD = new BigDecimal(usdRateInfo.getBaseRate().replace(",", ""));

        // 환전 내역 저장 및 결과 반환
        // DB에는 API 원본 환율을 저장
        saveExchangeHistory(request, fromAmount, toAmount, baseRateFromApi);
        return new ExchangeRes(
                request.fromCurrency(),
                toCurrency,
                appliedRate.setScale(2, RoundingMode.HALF_UP),
                fromAmount,
                exchangeRateUSD
        );

    }

    /**
     * 외화에서 원화로의 환전 처리
     */
    private ExchangeRes handleForeignToKrwExchange(ExchangeReq request, List<ExchangeRateRes> rates) {
        // 출발 통화 코드
        CurrencyCode fromCurrency = request.fromCurrency();

        // 출발 외화의 매매 기준율 정보 조회
        ExchangeRateRes rateInfo = findRate(rates, fromCurrency);
        BigDecimal baseRateFromApi = new BigDecimal(rateInfo.getBaseRate().replace(",", ""));

        // 통화 단위(unit)를 고려하여 기준 환율 조정 (예: JPY(100) -> 1 JPY에 대한 환율)
        BigDecimal adjustedBaseRate = baseRateFromApi.divide(new BigDecimal(fromCurrency.getUnit()), 4, RoundingMode.HALF_UP);

        // 팔 때 환율 계산: 조정된 매매기준율 * (1 - (스프레드 * (1 - 우대율)))
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = adjustedBaseRate.multiply(BigDecimal.ONE.subtract(effectiveSpread));

        // 외화 기준 금액
        BigDecimal fromAmount = request.targetAmount();
        // 입금될 원화 금액 계산
        BigDecimal krwReceived = fromAmount.multiply(appliedRate).setScale(0, RoundingMode.FLOOR);

        // USD 환율 정보 조회
        ExchangeRateRes usdRateInfo = findRate(rates, CurrencyCode.USD);
        BigDecimal exchangeRateUSD = new BigDecimal(usdRateInfo.getBaseRate().replace(",", ""));

        // 환전 내역 저장 및 결과 반환
        saveExchangeHistory(request, fromAmount, krwReceived, baseRateFromApi);
        return new ExchangeRes(
                fromCurrency,
                request.toCurrency(),
                appliedRate.setScale(2, RoundingMode.HALF_UP),
                fromAmount,
                exchangeRateUSD
        );
    }

    /**
     * 전체 환율 리스트에서 특정 통화에 해당하는 환율 정보를 찾는 메서드
     */
    private ExchangeRateRes findRate(List<ExchangeRateRes> rates, CurrencyCode currency) {
        String targetCode = currency.getCode();
        return rates.stream()
                .filter(r -> {
                    String apiCurrencyUnit = r.getCurrencyUnit();
                    if (apiCurrencyUnit == null) {
                        return false;
                    }
                    // "JPY(100)" -> "JPY", "IDR(100)" -> "IDR"
                    String parsedApiCode = apiCurrencyUnit.replaceAll("\\(\\d+\\)", "").trim();
                    return parsedApiCode.equalsIgnoreCase(targetCode);
                })
                .findFirst()
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED));
    }

    /**
     * 환전 내역을 데이터베이스에 저장
     */
    private void saveExchangeHistory(ExchangeReq exchangeReq, BigDecimal fromAmount, BigDecimal toAmount, BigDecimal rate) {
        Exchange exchange = Exchange.of(
                exchangeReq,
                fromAmount,
                toAmount,
                rate
        );
        exchangeRepository.save(exchange);
    }
}
