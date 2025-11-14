package org.creditto.core_banking.domain.exchange;

import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ExchangeServiceTest {

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @Mock
    private ExchangeRepository exchangeRepository;

    @InjectMocks
    private ExchangeService exchangeService;

    private ExchangeRateRes usdRate;
    private ExchangeRateRes jpyRate;

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
                .currencyUnit("JPY")
                .baseRate("900.00")
                .currencyName("일본 옌")
                .build();
    }

    @Test
    @DisplayName("환전 성공: 원화 -> 외화 (100 USD 요청)")
    void exchange_KRWToForeign_Success() {
        // Given
        BigDecimal targetUsdAmount = new BigDecimal("100.00");
        ExchangeReq request = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.USD, targetUsdAmount);

        // 서비스의 실제 계산 로직을 테스트 코드에 반영
        BigDecimal baseRate = new BigDecimal(usdRate.getBaseRate());
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.add(effectiveSpread)); // 살 때 환율
        BigDecimal expectedKrwDebit = targetUsdAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING); // 100 * (1300 * 1.005) = 130,650

        given(exchangeRateProvider.getExchangeRates()).willReturn(List.of(usdRate, jpyRate));

        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        // 1. 응답 검증
        assertThat(response.fromCurrency()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.toCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(response.exchangeAmount()).isEqualByComparingTo(expectedKrwDebit);

        // 2. 환전 내역 저장 여부 검증
        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("환전 성공: 외화 -> 원화 (100 USD 요청)")
    void exchange_ForeignToKRW_Success() {
        // Given
        BigDecimal usdAmount = new BigDecimal("100.00");
        ExchangeReq request = new ExchangeReq(CurrencyCode.USD, CurrencyCode.KRW, usdAmount);

        // 서비스의 실제 계산 로직을 테스트 코드에 반영
        BigDecimal baseRate = new BigDecimal(usdRate.getBaseRate());
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.subtract(effectiveSpread)); // 팔 때 환율

        given(exchangeRateProvider.getExchangeRates()).willReturn(List.of(usdRate, jpyRate));

        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        // 1. 응답 검증
        assertThat(response.fromCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(response.toCurrency()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.exchangeAmount()).isEqualByComparingTo(usdAmount);

        // 2. 환전 내역 저장 여부 검증
        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("지원하지 않는 통화로 환전 요청 시 실패")
    void exchange_Fail_UnsupportedCurrency() {
        // Given
        ExchangeReq request = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.EUR, new BigDecimal("100.00"));

        given(exchangeRateProvider.getExchangeRates()).willReturn(List.of(usdRate, jpyRate));

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(CustomBaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
    }
}