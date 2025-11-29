package org.creditto.core_banking.domain.account.dto;

import java.math.BigDecimal;

public record AccountSummaryRes(
        long accountCount,
        BigDecimal totalBalance
) {
}
