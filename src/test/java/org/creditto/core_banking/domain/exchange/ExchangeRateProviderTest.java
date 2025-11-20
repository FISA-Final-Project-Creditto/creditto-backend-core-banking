package org.creditto.core_banking.domain.exchange;

import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.global.feign.ExchangeRateFeign;
import org.creditto.core_banking.global.feign.ExchangeRateProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExchangeRateProviderTest {

    @Mock
    private ExchangeRateFeign exchangeRateFeign;

    @InjectMocks
    private ExchangeRateProvider exchangeRateProvider;

    @BeforeEach
    void setUp() {
        // wire test auth key since @Value won't run without Spring context
        ReflectionTestUtils.setField(exchangeRateProvider, "authkey", "test-key");
    }

    @Test
    @DisplayName("외부 환율 API 호출 및 데이터 수신 테스트")
    void getExchangeRates_APICall_Success() {
        List<ExchangeRateRes> mockedRates = List.of(
                ExchangeRateRes.builder()
                        .currencyUnit("USD")
                        .baseRate("1300.00")
                        .currencyName("미국 달러")
                        .build(),
                ExchangeRateRes.builder()
                        .currencyUnit("EUR")
                        .baseRate("1400.00")
                        .currencyName("유로")
                        .build()
        );

        // when: 외부 API 호출 대신 Mocked FeignClient 응답 사용
        given(exchangeRateFeign.getExchangeRate(anyString(), anyString(), eq("AP01")))
                .willReturn(mockedRates);

        Map<String, ExchangeRateRes> rates = exchangeRateProvider.getExchangeRates();

        assertThat(rates)
                .isNotNull()
                .hasSize(mockedRates.size())
                .containsKeys("USD", "EUR");
        assertThat(rates.get("USD").getBaseRate()).isEqualTo("1300.00");
        assertThat(rates.get("EUR").getCurrencyName()).isEqualTo("유로");
    }
}
