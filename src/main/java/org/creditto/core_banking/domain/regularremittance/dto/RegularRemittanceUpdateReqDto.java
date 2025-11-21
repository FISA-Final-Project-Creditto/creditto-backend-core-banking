package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.creditto.core_banking.domain.regularremittance.entity.RegRemStatus;
import org.creditto.core_banking.domain.regularremittance.entity.ScheduledDay;
import org.creditto.core_banking.global.common.CurrencyCode; // CurrencyCode import 추가

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class RegularRemittanceUpdateReqDto {
    private Long accountId;
    private Long recipientId;
    private CurrencyCode sendCurrency; // String -> CurrencyCode
    private CurrencyCode receivedCurrency; // String -> CurrencyCode
    private BigDecimal sendAmount;
    private RegRemStatus regRemStatus;

    private String regRemType;      // MONTHLY/WEEKLY

    private Integer scheduledDate;  // 매월 송금
    private ScheduledDay scheduledDay;    // 매주 송금
}
