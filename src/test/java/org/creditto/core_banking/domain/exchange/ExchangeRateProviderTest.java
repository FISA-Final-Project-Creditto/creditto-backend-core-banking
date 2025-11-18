package org.creditto.core_banking.domain.exchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.global.feign.ExchangeRateProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExchangeRateProviderTest {

    @Autowired
    private ExchangeRateProvider exchangeRateProvider;

    @Autowired
    private ObjectMapper objectMapper; // JSON 출력을 위해 ObjectMapper 주입

    @Test
    @DisplayName("외부 환율 API 호출 및 데이터 수신 테스트")
    void getExchangeRates_APICall_Success() {
        // when: ExchangeRateProvider를 통해 실제 외부 API 호출
        Map<String, ExchangeRateRes> rates = exchangeRateProvider.getExchangeRates();

        assertThat(rates).isNotNull().isNotEmpty();

        System.out.println("Successfully fetched " + rates.size() + " exchange rates.");
        rates.values().forEach(rate -> {
            try {
                // 각 환율 정보를 보기 좋은 JSON 형태로 출력
                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rate));
            } catch (JsonProcessingException e) {
                System.out.println("Error printing rate object: " + e.getMessage());
            }
        });
    }
}
