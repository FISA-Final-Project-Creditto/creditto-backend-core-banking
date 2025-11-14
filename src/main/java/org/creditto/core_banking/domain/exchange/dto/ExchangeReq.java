package org.creditto.core_banking.domain.exchange.dto;

import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;

public record ExchangeReq(
    CurrencyCode fromCurrency,
    CurrencyCode toCurrency,
    BigDecimal targetAmount
) {
}
