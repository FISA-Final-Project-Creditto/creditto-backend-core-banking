package org.creditto.core_banking.domain.remittancefee.dto;

import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;

public record RemittanceFeeReq(
        BigDecimal exchangeRate,        // 제공 환율
        BigDecimal sendAmount,          // 송금 금액
        CurrencyCode currency,          // 통화 코드
        BigDecimal fromAmountInUSD      // 송금 금액 -> USD
) {
}
