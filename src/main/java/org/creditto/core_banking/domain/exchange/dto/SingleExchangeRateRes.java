package org.creditto.core_banking.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SingleExchangeRateRes {

    private final String currencyCode; // 통화 코드
    private final String baseRate; // 매매 기준율

    public static SingleExchangeRateRes from(ExchangeRateRes exchangeRateRes) {
        return SingleExchangeRateRes.builder()
            .currencyCode(exchangeRateRes.getCurrencyUnit())
            .baseRate(exchangeRateRes.getBaseRate())
            .build();
    }
}
