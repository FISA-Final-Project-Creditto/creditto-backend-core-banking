package org.creditto.core_banking.domain.exchange;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
import org.creditto.core_banking.domain.exchange.repository.ExchangeRepository;
import org.creditto.core_banking.domain.exchange.service.ExchangeServiceImpl;
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
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ExchangeServiceImplTest {

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRepository exchangeRepository;

    @InjectMocks
    private ExchangeServiceImpl exchangeService;

    private Account testAccount;
    private ExchangeRateRes usdRate;
    private ExchangeRateRes jpyRate;

    @BeforeEach
    void setUp() {
        testAccount = Account.of(
                "1234567890",
                "Test Account",
                new BigDecimal("1000000.00"), // 1,000,000 KRW
                null, null, "client123"
        );

        // 1 USD = 1300 KRW (매매 기준율)
        // 살 때 (서비스가 외화를 살 때) = 1280 KRW
        // 팔 때 (서비스가 외화를 팔 때) = 1320 KRW
        usdRate = ExchangeRateRes.builder()
                .currencyUnit("USD")
                .baseRate("1300.00")
                .buyRate("1280.00")
                .sellRate("1320.00")
                .build();

        // 1 JPY = 9 KRW (매매 기준율)
        // 살 때 (서비스가 외화를 살 때) = 8.8 KRW
        // 팔 때 (서비스가 외화를 팔 때) = 9.2 KRW
        jpyRate = ExchangeRateRes.builder()
                .currencyUnit("JPY")
                .baseRate("9.00")
                .buyRate("8.80")
                .sellRate("9.20")
                .build();
    }

    @Test
    @DisplayName("환전 성공: 원화 -> 외화 (100 USD 요청)")
    void exchange_KRWTo_Success() {
        // Given
        ExchangeReq request = ExchangeReq.builder()
                .accountId(1L)
                .fromCurrency("KRW")
                .toCurrency("USD")
                .targetAmount(new BigDecimal("100.00")) // 100 USD 요청
                .build();

        BigDecimal sellRate = new BigDecimal(usdRate.getSellRate()); // 1320.00
        BigDecimal expectedKrwDebit = request.getTargetAmount().multiply(sellRate); // 100 * 1320 = 132,000 KRW

        given(exchangeRateProvider.getExchangeRates()).willReturn(Arrays.asList(usdRate, jpyRate));
        given(accountRepository.findById(1L)).willReturn(Optional.of(testAccount));

        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        assertThat(response.getExchangeAmount()).isEqualByComparingTo("100.00"); // 받은 외화 금액은 100 USD
        BigDecimal expectedRemainingBalance = new BigDecimal("1000000.00").subtract(expectedKrwDebit); // 1,000,000 - 132,000 = 868,000 KRW
        assertThat(testAccount.getBalance()).isEqualByComparingTo(expectedRemainingBalance);
    }

    @Test
    @DisplayName("환전 성공: 외화 -> 원화 (100000 KRW 요청)")
    void exchange_ToKRW_Success() {
        // Given
        ExchangeReq request = ExchangeReq.builder()
                .accountId(1L)
                .fromCurrency("USD")
                .toCurrency("KRW")
                .targetAmount(new BigDecimal("100000.00")) // 100,000 KRW 요청
                .build();

        BigDecimal buyRate = new BigDecimal(usdRate.getBuyRate()); // 1280.00

        given(exchangeRateProvider.getExchangeRates()).willReturn(Arrays.asList(usdRate, jpyRate));
        given(accountRepository.findById(1L)).willReturn(Optional.of(testAccount));

        // When
        ExchangeRes response = exchangeService.exchange(request);

        // Then
        assertThat(response.getExchangeAmount()).isEqualByComparingTo("100000.00"); // 받은 원화 금액은 100,000 KRW
        BigDecimal expectedNewBalance = new BigDecimal("1000000.00").add(request.getTargetAmount()); // 1,000,000 + 100,000 = 1,100,000 KRW
        assertThat(testAccount.getBalance()).isEqualByComparingTo(expectedNewBalance);
    }

    @Test
    @DisplayName("잔액 부족으로 인한 환전 실패: 원화 -> 외화")
    void exchange_Fail_InsufficientBalance() {
        // Given
        ExchangeReq request = ExchangeReq.builder()
                .accountId(1L)
                .fromCurrency("KRW")
                .toCurrency("USD")
                .targetAmount(new BigDecimal("1000.00")) // 1000 USD 요청
                .build();

        given(exchangeRateProvider.getExchangeRates()).willReturn(Arrays.asList(usdRate, jpyRate));
        given(accountRepository.findById(1L)).willReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("계좌 잔액이 부족합니다.");

        // Verify no changes were made
        assertThat(testAccount.getBalance()).isEqualByComparingTo("1000000.00");
    }

    @Test
    @DisplayName("지원하지 않는 통화로 환전 요청 시 실패")
    void exchange_Fail_UnsupportedCurrency() {
        // Given
        ExchangeReq request = ExchangeReq.builder()
                .accountId(1L)
                .fromCurrency("KRW")
                .toCurrency("EUR") // 지원하지 않는 통화라고 가정
                .targetAmount(new BigDecimal("100.00"))
                .build();

        given(exchangeRateProvider.getExchangeRates()).willReturn(Arrays.asList(usdRate, jpyRate));
        given(accountRepository.findById(1L)).willReturn(Optional.of(testAccount));

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("해당 통화(EUR)의 환율 정보를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 계좌로 환전 요청 시 실패")
    void exchange_Fail_AccountNotFound() {
        // Given
        ExchangeReq request = ExchangeReq.builder()
                .accountId(99L) // 존재하지 않는 계좌 ID
                .fromCurrency("KRW")
                .toCurrency("USD")
                .targetAmount(new BigDecimal("100.00"))
                .build();

        given(exchangeRateProvider.getExchangeRates()).willReturn(Arrays.asList(usdRate, jpyRate));
        given(accountRepository.findById(99L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> exchangeService.exchange(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage());
    }
}