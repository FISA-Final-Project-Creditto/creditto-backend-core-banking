package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.overseasremittance.entity.RemittanceStatus;
import org.creditto.core_banking.domain.regularremittance.entity.ScheduledDay;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RemittanceHistoryResDto {
    Long remittanceId;
    Long regRemId;

    String recipientName;
    String recipientBankName;

    CurrencyCode sendCurrency;
    BigDecimal sendAmount;
    CurrencyCode receivedCurrency;
    BigDecimal receivedAmount;
    RemittanceStatus remittanceStatus;  // 송금 처리 상태

    String regRemType;
    Integer scheduledDate;
    ScheduledDay scheduledDay;

    LocalDateTime createdAt;


}
