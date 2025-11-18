package org.creditto.core_banking.global.feign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.global.common.CurrencyCode;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateProvider {

    private final ExchangeRateFeign exchangeRateFeign;

    @Value("${exchange.auth-key}")
    private String authkey;


    /**
     * 한국수출입은행 API에서 제공하는 환율 정보를 조회하여 Map 형태로 반환
     *
     * @return 조회된 환율 정보 Map (Key: 통화코드, Value: 환율정보 DTO)
     * @throws RuntimeException API 호출 실패 또는 최종 데이터 조회 실패 시 발생
     */
    public Map<String, ExchangeRateRes> getExchangeRates() { // Changed return type
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<ExchangeRateRes> rates;

        try {
            rates = exchangeRateFeign.getExchangeRate(authkey, today, "AP01");

            // 비영업일이거나 null 응답을 받으면 전 영업일로 재조회
            if (isInvalidResponse(rates)) { // Using helper method
                LocalDate exDate = getPreviousBusinessDate(LocalDate.now());
                String newDate = exDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                rates = exchangeRateFeign.getExchangeRate(authkey, newDate, "AP01");
            }

            // 그래도 null일 경우 강제 예외
            if (isInvalidResponse(rates)) { // Using helper method
                throw new RuntimeException(ErrorBaseCode.INTERNAL_SERVER_ERROR.getMessage());
            }

            // 최종적으로 조회된 List를 Map으로 변환하여 반환
            return rates.stream()
                    .collect(Collectors.toMap(
                            ExchangeRateRes::getCurrencyUnit,
                            rate -> rate,
                            (existing, replacement) -> existing
                    ));

        } catch (Exception e) {
            throw new RuntimeException(ErrorBaseCode.INTERNAL_SERVER_ERROR.getMessage(), e);
        }
    }

    // 응답 유효성 검사 로직을 별도 메서드로 추출
    private boolean isInvalidResponse(List<ExchangeRateRes> rates) {
        return rates == null || rates.isEmpty() || rates.get(0).getCurrencyUnit() == null;
    }

    /**
     * 주어진 날짜를 기준으로 가장 가까운 이전 영업일 조회
     *
     * @param date 기준 날짜
     * @return 주말이 아닌 가장 최근의 날짜
     */
    private LocalDate getPreviousBusinessDate(LocalDate date) {
        LocalDate prev = date.minusDays(1);
        while (prev.getDayOfWeek() == DayOfWeek.SATURDAY || prev.getDayOfWeek() == DayOfWeek.SUNDAY) {
            prev = prev.minusDays(1);
        }
        return prev;
    }
}
