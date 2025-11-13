package org.creditto.core_banking.domain.remittancefee.dto;

import java.math.BigDecimal;

public record RemittanceFeeReq(
    BigDecimal exchangeRate,
    BigDecimal sendAmount,
    String currency
) {
}
