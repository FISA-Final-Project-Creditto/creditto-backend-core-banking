package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class RemittanceHistoryDto {
    private Long remittanceId;
    private BigDecimal sendAmount;
    private BigDecimal exchangeRate;
    private LocalDate createdDate;
}
