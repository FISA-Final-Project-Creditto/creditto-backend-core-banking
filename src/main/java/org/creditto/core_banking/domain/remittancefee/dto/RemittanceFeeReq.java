package org.creditto.core_banking.domain.remittancefee.dto;

import java.math.BigDecimal;

public record RemittanceFeeReq(
    BigDecimal exchangeRate,        // 제공 환율
    BigDecimal sendAmount,          // 송금 금액
    String currency,                // 통화 코드
    BigDecimal exchangeRateUSD      // 달러 환율
) {
}
