package org.creditto.core_banking.domain.exchange.dto;


import java.math.BigDecimal;

// 내부 환전 결과 반환 DTO
public record ExchangeRes (
    String fromCurrency,
    String toCurrency,
    String country,
    BigDecimal exchangeRate,
    BigDecimal exchangeAmount
) {
}
