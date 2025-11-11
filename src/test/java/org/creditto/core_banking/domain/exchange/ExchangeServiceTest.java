package org.creditto.core_banking.domain.exchange;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
import org.creditto.core_banking.domain.exchange.repository.ExchangeRepository;
import org.creditto.core_banking.domain.exchange.service.ExchangeService;
import org.creditto.core_banking.global.feign.ExchangeRateProvider;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRepository exchangeRepository;

    @InjectMocks
    private ExchangeService exchangeService;

    private Account testAccount;
    private ExchangeRateRes usdRate;
    private ExchangeRateRes jpyRate;

    // 테스트에 사용할 상수 정의
    private static final BigDecimal SPREAD_RATE = new BigDecimal("0.01");
    private static final BigDecimal PREFERENTIAL_RATE = new BigDecimal("0.5");

    @BeforeEach
    void setUp() {
        testAccount = Account.of(
                "1234567890",
                "Test Account",
                new BigDecimal("1000000.00"), // 1,000,000 KRW
                null, null, "client123"
        );

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
        long accountId = 1L;
        BigDecimal targetUsdAmount = new BigDecimal("100.00");
        ExchangeReq request = new ExchangeReq(accountId, "KRW", "USD", "US", targetUsdAmount);

        // 서비스의 실제 계산 로직을 테스트 코드에 반영
        BigDecimal baseRate = new BigDecimal(usdRate.getBaseRate());
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.add(effectiveSpread)); // 살 때 환율
        BigDecimal expectedKrwDebit = targetUsdAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING); // 100 * (1300 * 1.005) = 130,650

        given(exchangeRateProvider.getExchangeRates()).willReturn(List.of(usdRate, jpyRate));
        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));

        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        // 1. 응답 검증
        assertThat(response.fromCurrency()).isEqualTo("KRW");
        assertThat(response.toCurrency()).isEqualTo("USD");
        assertThat(response.exchangeAmount()).isEqualByComparingTo(expectedKrwDebit);

        // 2. 계좌 잔액 변경 검증
        BigDecimal expectedRemainingBalance = new BigDecimal("1000000").subtract(expectedKrwDebit); // 1,000,000 - 130,650 = 869,350
        assertThat(testAccount.getBalance()).isEqualByComparingTo(expectedRemainingBalance);

        // 3. 환전 내역 저장 여부 검증
        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("환전 성공: 외화 -> 원화 (100 USD 요청)")
    void exchange_ForeignToKRW_Success() {
        // Given
        long accountId = 1L;
        BigDecimal usdAmount = new BigDecimal("100.00");
        ExchangeReq request = new ExchangeReq(accountId, "USD", "KRW", "KR", usdAmount);

        // 서비스의 실제 계산 로직을 테스트 코드에 반영
        BigDecimal baseRate = new BigDecimal(usdRate.getBaseRate());
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.subtract(effectiveSpread)); // 팔 때 환율
        BigDecimal expectedKrwCredit = usdAmount.multiply(appliedRate).setScale(0, RoundingMode.FLOOR); // 100 * (1300 * 0.995) = 129,350

        given(exchangeRateProvider.getExchangeRates()).willReturn(List.of(usdRate, jpyRate));
        given(accountRepository.findById(accountId)).willReturn(Optional.of(testAccount));

        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        // 1. 응답 검증
        assertThat(response.fromCurrency()).isEqualTo("USD");
        assertThat(response.toCurrency()).isEqualTo("KRW");
        assertThat(response.exchangeAmount()).isEqualByComparingTo(usdAmount);

        // 2. 계좌 잔액 변경 검증
        BigDecimal expectedNewBalance = new BigDecimal("1000000").add(expectedKrwCredit); // 1,000,000 + 129,350 = 1,129,350
        assertThat(testAccount.getBalance()).isEqualByComparingTo(expectedNewBalance);

        // 3. 환전 내역 저장 여부 검증
        verify(exchangeRepository).save(any());
    }

    @Test
    @DisplayName("잔액 부족으로 인한 환전 실패: 원화 -> 외화")
    void exchange_Fail_InsufficientBalance() {
        // Given
        // 1,000,000 KRW 잔액으로 1000 USD(약 1,306,500 KRW) 환전 요청
        ExchangeReq request = new ExchangeReq(1L, "KRW", "USD", "US", new BigDecimal("1000.00"));

        given(exchangeRateProvider.getExchangeRates()).willReturn(List.of(usdRate));
        given(accountRepository.findById(1L)).willReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage());
    }

    @Test
    @DisplayName("지원하지 않는 통화로 환전 요청 시 실패")
    void exchange_Fail_UnsupportedCurrency() {
        // Given
        ExchangeReq request = new ExchangeReq(1L, "KRW", "EUR", "DE", new BigDecimal("100.00"));

        given(exchangeRateProvider.getExchangeRates()).willReturn(List.of(usdRate, jpyRate));

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 통화(EUR)의 환율 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 계좌로 환전 요청 시 실패")
    void exchange_Fail_AccountNotFound() {
        // Given
        long nonExistentAccountId = 99L;
        ExchangeReq request = new ExchangeReq(nonExistentAccountId, "KRW", "USD", "US", new BigDecimal("100.00"));

        given(accountRepository.findById(nonExistentAccountId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage());
    }
}