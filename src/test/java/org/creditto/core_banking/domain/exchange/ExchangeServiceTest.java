package org.creditto.core_banking.domain.exchange;

import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
import org.creditto.core_banking.domain.exchange.dto.SingleExchangeRateRes;
import org.creditto.core_banking.domain.exchange.entity.Exchange;
import org.creditto.core_banking.domain.exchange.repository.ExchangeRepository;
import org.creditto.core_banking.domain.exchange.service.ExchangeService;
import org.creditto.core_banking.domain.creditscore.service.CreditScoreService;
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

    @Mock
    private CreditScoreService creditScoreService;

    @InjectMocks
    private ExchangeService exchangeService;

    private ExchangeRateRes usdRate;
    private ExchangeRateRes jpyRate;
    private Map<String, ExchangeRateRes> rateMap;

    private static final BigDecimal SPREAD_RATE = new BigDecimal("0.01");
    private static final BigDecimal MOCK_PREFERENTIAL_RATE = new BigDecimal("0.5");


    @BeforeEach
    void setUp() {
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
        Long userId = 1L;
        BigDecimal targetUsdAmount = new BigDecimal("100.00");
        ExchangeReq request = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.USD, targetUsdAmount);
        Exchange mockExchange = Exchange.builder().id(1L).build();

        BigDecimal baseRate = new BigDecimal(usdRate.getBaseRate());
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(MOCK_PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.add(effectiveSpread));
        BigDecimal expectedKrwDebit = targetUsdAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING);

        BigDecimal expectedFromAmountInUSD = expectedKrwDebit.divide(new BigDecimal(usdRate.getBaseRate()), 2, RoundingMode.HALF_UP);

        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);
        given(exchangeRepository.save(any(Exchange.class))).willReturn(mockExchange);
        given(creditScoreService.getPreferentialRate(userId)).willReturn(MOCK_PREFERENTIAL_RATE.doubleValue());

        // When
        ExchangeRes response = exchangeService.exchange(userId, request);

        // Then
        assertThat(response.fromCurrency()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.toCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(response.exchangeAmount()).isEqualByComparingTo(expectedKrwDebit);
        assertThat(response.fromAmountInUSD()).isEqualByComparingTo(expectedFromAmountInUSD);

        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("환전 성공: 외화 -> 원화 (100,000 KRW 수취 요청)")
    void exchange_ForeignToKRW_Success() {
        // Given
        Long userId = 1L;
        BigDecimal targetKrwAmount = new BigDecimal("100000");
        ExchangeReq request = new ExchangeReq(CurrencyCode.USD, CurrencyCode.KRW, targetKrwAmount);
        Exchange mockExchange = Exchange.builder().id(1L).build();

        BigDecimal baseRate = new BigDecimal(usdRate.getBaseRate());
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(MOCK_PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.subtract(effectiveSpread));
        BigDecimal expectedFromAmount = targetKrwAmount.divide(appliedRate, 2, RoundingMode.CEILING);
        BigDecimal expectedFromAmountInUSD = expectedFromAmount;

        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);
        given(exchangeRepository.save(any(Exchange.class))).willReturn(mockExchange);
        given(creditScoreService.getPreferentialRate(userId)).willReturn(MOCK_PREFERENTIAL_RATE.doubleValue());

        // When
        ExchangeRes response = exchangeService.exchange(userId, request);

        // Then
        assertThat(response.fromCurrency()).isEqualTo(CurrencyCode.USD);
        assertThat(response.toCurrency()).isEqualTo(CurrencyCode.KRW);
        assertThat(response.exchangeAmount()).isEqualByComparingTo(expectedFromAmount);
        assertThat(response.fromAmountInUSD()).isEqualByComparingTo(expectedFromAmountInUSD);

        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("환전 성공: 원화 -> 외화 (100 JPY 요청)")
    void exchange_KRWToJPY_Success() {
        // Given
        Long userId = 1L;
        BigDecimal targetJpyAmount = new BigDecimal("100.00");
        ExchangeReq request = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.JPY, targetJpyAmount);
        Exchange mockExchange = Exchange.builder().id(1L).build();

        BigDecimal baseRatePerUnit = new BigDecimal(jpyRate.getBaseRate()).divide(new BigDecimal(CurrencyCode.JPY.getUnit()), 4, RoundingMode.HALF_UP);
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(MOCK_PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRatePerUnit.multiply(BigDecimal.ONE.add(effectiveSpread));
        BigDecimal expectedKrwDebit = targetJpyAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING);
        BigDecimal expectedFromAmountInUSD = expectedKrwDebit.divide(new BigDecimal(usdRate.getBaseRate()), 2, RoundingMode.HALF_UP);

        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);
        given(exchangeRepository.save(any(Exchange.class))).willReturn(mockExchange);
        given(creditScoreService.getPreferentialRate(userId)).willReturn(MOCK_PREFERENTIAL_RATE.doubleValue());

        // When
        ExchangeRes response = exchangeService.exchange(userId, request);

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
        Long userId = 1L;
        ExchangeReq request = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.EUR, new BigDecimal("100.00"));

        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);
        given(creditScoreService.getPreferentialRate(userId)).willReturn(MOCK_PREFERENTIAL_RATE.doubleValue());

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(userId, request))
                .isInstanceOf(CustomBaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
    }

    @Test
    @DisplayName("특정 통화(USD) 환율 조회 성공")
    void getRateByCurrency_Success() {
        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);

        SingleExchangeRateRes result = exchangeService.getRateByCurrency(CurrencyCode.USD);

        assertThat(result).isNotNull();
        assertThat(result.getCurrencyCode()).isEqualTo("USD");
        assertThat(result.getExchangeRate()).isEqualByComparingTo(new BigDecimal("1300.00"));
    }

    @Test
    @DisplayName("지원하지 않는 통화(EUR) 환율 조회 시 실패")
    void getRateByCurrency_NotFound_ThrowsException() {
        given(exchangeRateProvider.getExchangeRates()).willReturn(rateMap);

        assertThatThrownBy(() -> exchangeService.getRateByCurrency(CurrencyCode.EUR))
            .isInstanceOf(CustomBaseException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
    }
}