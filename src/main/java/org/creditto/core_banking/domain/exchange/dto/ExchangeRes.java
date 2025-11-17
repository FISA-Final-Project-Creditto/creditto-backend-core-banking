package org.creditto.core_banking.domain.exchange.dto;


import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;

// 내부 환전 결과 반환 DTO
public record ExchangeRes (
    CurrencyCode fromCurrency,
    CurrencyCode toCurrency,
    BigDecimal exchangeRate,
    BigDecimal exchangeAmount,
    BigDecimal exchangeRateUSD // USD 환율
) {
}
