package org.creditto.core_banking.domain.exchange;

import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
import org.creditto.core_banking.domain.exchange.dto.SingleExchangeRateRes;
import org.creditto.core_banking.domain.exchange.entity.Exchange;
import org.creditto.core_banking.domain.exchange.repository.ExchangeRepository;
import org.creditto.core_banking.domain.exchange.service.ExchangeService;
import org.creditto.core_banking.global.common.CurrencyCode;
import org.creditto.core_banking.global.feign.ExchangeRateProvider;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class  ExchangeServiceTest {

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @Mock
    private ExchangeRepository exchangeRepository;

    @InjectMocks
    private ExchangeService exchangeService;

    private ExchangeRateRes usdRate;
    private ExchangeRateRes jpyRate;
    private Map<String, ExchangeRateRes> rateMap;

    // 테스트에 사용할 상수 정의
    private static final BigDecimal SPREAD_RATE = new BigDecimal("0.01");
    private static final BigDecimal PREFERENTIAL_RATE = new BigDecimal("0.5");

    @BeforeEach
    void setUp() {
        // Mock DTO 설정: Builder 패턴을 사용하여 객체 생성
        usdRate = ExchangeRateRes.builder()
                .result(1)
                .currencyUnit("USD")
                .baseRate("1300.00")
                .currencyName("미국 달러")
                .build();

        jpyRate = ExchangeRateRes.builder()
                .result(1)
                .currencyUnit("JPY(100)")
                .baseRate("900.00")
                .currencyName("일본 옌")
                .build();

        rateMap = Map.of(
                "USD", usdRate,
                "JPY(100)", jpyRate
        );
    }

    @Test
    @DisplayName("환전 성공: 원화 -> 외화 (100 USD 요청)")
    void exchange_KRWToForeign_Success() {
        // Given
        BigDecimal targetUsdAmount = new BigDecimal("100.00");
        ExchangeReq request = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.USD, targetUsdAmount);
        Exchange mockExchange = Exchange.builder().id(1L).build();

        // 서비스의 실제 계산 로직을 테스트 코드에 반영
        BigDecimal baseRate = new BigDecimal(usdRate.getBaseRate());
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.add(effectiveSpread)); // 살 때 환율
        BigDecimal expectedKrwDebit = targetUsdAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING); // 100 * (1300 * 1.005) = 130,650

        // fromAmountInUSD 계산 (KRW -> USD)
        // fromAmount는 expectedKrwDebit (원화)
        // KRW의 기준 환율은 1
        // USD의 기준 환율은 usdRate.getBaseRate()
        BigDecimal expectedFromAmountInUSD = expectedKrwDebit.divide(new BigDecimal(usdRate.getBaseRate()), 2, RoundingMode.HALF_UP);

        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);
        given(exchangeRepository.save(any(Exchange.class))).willReturn(mockExchange);


        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        // 1. 응답 검증
        assertThat(response.fromCurrency()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.toCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(response.exchangeAmount()).isEqualByComparingTo(expectedKrwDebit);
        assertThat(response.fromAmountInUSD()).isEqualByComparingTo(expectedFromAmountInUSD);

        // 2. 환전 내역 저장 여부 검증
        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("환전 성공: 외화 -> 원화 (100,000 KRW 수취 요청)")
    void exchange_ForeignToKRW_Success() {
        // Given
        BigDecimal targetKrwAmount = new BigDecimal("100000"); // 100,000 KRW를 받고 싶음
        ExchangeReq request = new ExchangeReq(CurrencyCode.USD, CurrencyCode.KRW, targetKrwAmount);
        Exchange mockExchange = Exchange.builder().id(1L).build();

        // 서비스의 실제 계산 로직을 테스트 코드에 반영
        BigDecimal baseRate = new BigDecimal(usdRate.getBaseRate());
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.subtract(effectiveSpread)); // 팔 때 환율: 1300 * 0.995 = 1293.5

        // 100,000 KRW를 받기 위해 필요한 USD(fromAmount)를 역산
        // fromAmount = 100000 / 1293.5 = 77.309... -> 77.31 (올림)
        BigDecimal expectedFromAmount = targetKrwAmount.divide(appliedRate, 2, RoundingMode.CEILING);

        // fromAmountInUSD 계산 (USD -> KRW)
        // fromAmount는 expectedFromAmount (USD)
        // USD의 기준 환율은 usdRate.getBaseRate()
        // USD의 USD 기준 환율은 usdRate.getBaseRate()
        // 따라서 fromAmountInUSD는 expectedFromAmount와 동일
        BigDecimal expectedFromAmountInUSD = expectedFromAmount;

        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);
        given(exchangeRepository.save(any(Exchange.class))).willReturn(mockExchange);

        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        // 1. 응답 검증
        assertThat(response.fromCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(response.toCurrency()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.exchangeAmount()).isEqualByComparingTo(expectedFromAmount); // 내야 할 외화 금액 검증
        assertThat(response.fromAmountInUSD()).isEqualByComparingTo(expectedFromAmountInUSD);

        // 2. 환전 내역 저장 여부 검증
        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("환전 성공: 원화 -> 외화 (100 JPY 요청)")
    void exchange_KRWToJPY_Success() {
        // Given
        BigDecimal targetJpyAmount = new BigDecimal("100.00"); // 100 JPY
        ExchangeReq request = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.JPY, targetJpyAmount);
        Exchange mockExchange = Exchange.builder().id(1L).build();

        // 서비스의 실제 계산 로직을 테스트 코드에 반영
        // JPY의 baseRate는 100 JPY당 900원 (jpyRate.baseRate)
        // 1 JPY당 환율은 900 / 100 = 9.00원
        BigDecimal baseRatePerUnit = new BigDecimal(jpyRate.getBaseRate()).divide(new BigDecimal(CurrencyCode.JPY.getUnit()), 4, RoundingMode.HALF_UP); // 900.00 / 100 = 9.00
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRatePerUnit.multiply(BigDecimal.ONE.add(effectiveSpread)); // 살 때 환율: 9.00 * (1 + 0.005) = 9.045
        BigDecimal expectedKrwDebit = targetJpyAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING); // 100 * 9.045 = 905

        // fromAmountInUSD 계산 (KRW -> JPY)
        // fromAmount는 expectedKrwDebit (원화)
        // KRW의 기준 환율은 1
        // USD의 기준 환율은 usdRate.getBaseRate()
        BigDecimal expectedFromAmountInUSD = expectedKrwDebit.divide(new BigDecimal(usdRate.getBaseRate()), 2, RoundingMode.HALF_UP);

        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);
        given(exchangeRepository.save(any(Exchange.class))).willReturn(mockExchange);

        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        assertThat(response.fromCurrency()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.toCurrency()).isEqualTo(CurrencyCode.JPY);
        assertThat(response.exchangeAmount()).isEqualByComparingTo(expectedKrwDebit);
        assertThat(response.fromAmountInUSD()).isEqualByComparingTo(expectedFromAmountInUSD);

        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("지원하지 않는 통화로 환전 요청 시 실패")
    void exchange_Fail_UnsupportedCurrency() {
        // Given
        ExchangeReq request = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.EUR, new BigDecimal("100.00"));

        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(CustomBaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
    }

    @Test
    @DisplayName("특정 통화(USD) 환율 조회 성공")
    void getRateByCurrency_Success() {
        // Given
        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);

        // When
        SingleExchangeRateRes result = exchangeService.getRateByCurrency(CurrencyCode.USD);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrencyCode()).isEqualTo("USD");
        assertThat(result.getBaseRate()).isEqualTo("1300.00");
    }

    @Test
    @DisplayName("지원하지 않는 통화(EUR) 환율 조회 시 실패")
    void getRateByCurrency_NotFound_ThrowsException() {
        // Given
        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);

        // When & Then
        assertThatThrownBy(() -> exchangeService.getRateByCurrency(CurrencyCode.EUR))
            .isInstanceOf(CustomBaseException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
    }
}