package org.creditto.core_banking.domain.exchange.dto;

import java.math.BigDecimal;

public record ExchangeReq(
    Long accountId,
    String fromCurrency,
    String toCurrency,
    String country,
    BigDecimal targetAmount
) {
}
