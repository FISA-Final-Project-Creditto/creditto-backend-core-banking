package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.creditto.core_banking.domain.regularremittance.entity.ScheduledDay;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class RegularRemittanceCreateReqDto {

    private Long accountId;
    private Long recipientId;
    private CurrencyCode sendCurrency;
    private CurrencyCode receivedCurrency;
    private BigDecimal sendAmount;

    private String regRemType;      // MONTHLY/WEEKLY

    private Integer scheduledDate;  // 매월 송금 (1-31)
    private ScheduledDay scheduledDay;    // 매주 송금 (MONDAY-SUNDAY)
}
