package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class RemittanceHistoryDto {
    Long remittanceId;
    BigDecimal sendAmount;
    BigDecimal exchangeRate;
    LocalDate createdDate;
}
