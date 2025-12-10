package org.creditto.core_banking.domain.exchange.controller;

import java.math.BigDecimal;
import org.creditto.core_banking.domain.creditscore.service.CreditScoreService;
import org.creditto.core_banking.domain.exchange.dto.PreferentialRateRes;
import org.creditto.core_banking.domain.exchange.service.ExchangeService;
import org.creditto.core_banking.global.common.CurrencyCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExchangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExchangeService exchangeService;

    @Autowired
    private CreditScoreService creditScoreService;

    @Test
    @DisplayName("우대 환율 정보 조회 성공")
    void getPreferentialRate_Success() throws Exception {
        Long userId = 1L;
        String currency = "USD";
        double preferentialRate = 0.5;
        BigDecimal appliedRate = new BigDecimal("1306.50");

        PreferentialRateRes mockResponse = new PreferentialRateRes(preferentialRate, appliedRate);

        given(exchangeService.getPreferentialRateInfo(userId, CurrencyCode.USD)).willReturn(mockResponse);

        mockMvc.perform(get("/api/core/exchange/preferential-rate/{userId}/{currency}", userId, currency))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.message").value("요청이 성공했습니다."))
                .andExpect(jsonPath("$.data.preferentialRate").value(preferentialRate))
                .andExpect(jsonPath("$.data.appliedRate").value(appliedRate.doubleValue()));
    }

    @TestConfiguration
    static class MockConfig {

        @Bean
        @Primary
        ExchangeService exchangeService() {
            return Mockito.mock(ExchangeService.class);
        }

        @Bean
        @Primary
        CreditScoreService creditScoreService() {
            return Mockito.mock(CreditScoreService.class);
        }
    }
}
