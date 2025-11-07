package org.creditto.core_banking.global.feign;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExchangeRateProvider {

    private final ExchangeRateFeign exchangeRateFeign;

    @Value("${exchange.auth-key}")
    private String authKey;


    /**
     * 한국수출입은행 API에서 제공하는 환율 정보를 조회
     *
     * @return 당일의 모든 국가 환율 정보 리스트
     * @throws RuntimeException - API 호출에 실패한 경우
     */
    public List<ExchangeRateRes> getExchangeRates() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<ExchangeRateRes> rates;

        try {
            rates = exchangeRateFeign.getExchangeRate(authKey, today, "AP01");

            // 비영업일이거나 null 응답을 받으면 전 영업일로 재조회
            if (rates == null || rates.isEmpty() || rates.get(0).getCurrencyUnit() == null) {
                LocalDate exDate = getPreviousBusinessDate(LocalDate.now());
                String newDate = exDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                rates = exchangeRateFeign.getExchangeRate(authKey, newDate, "AP01");
            }

            // 그래도 null일 경우 강제 예외
            if (rates == null || rates.isEmpty()) {
                throw new RuntimeException(ErrorBaseCode.INTERNAL_SERVER_ERROR.getMessage());
            }

            return rates;

        } catch (Exception e) {
            throw new RuntimeException(ErrorBaseCode.INTERNAL_SERVER_ERROR.getMessage());
        }

    }

    /**
     * 오전 11시 이전 API 요청 시, null 반환 -> 이전 영업일 계산 메서드
     * 주말을 제외한 전 영업일 계산
     */
    private LocalDate getPreviousBusinessDate(LocalDate date) {
        LocalDate prev = date.minusDays(1);
        while (prev.getDayOfWeek() == DayOfWeek.SATURDAY || prev.getDayOfWeek() == DayOfWeek.SUNDAY) {
            prev = prev.minusDays(1);
        }
        return prev;
    }
}
