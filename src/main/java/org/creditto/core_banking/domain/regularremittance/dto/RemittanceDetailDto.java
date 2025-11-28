package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.overseasremittance.entity.RemittanceStatus;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class RemittanceDetailDto {
    private String accountNo;
    private BigDecimal sendAmount;
    private String regRemType;      // 매월, 매주
    private Integer scheduledDate;
    private DayOfWeek scheduledDay;
    private LocalDate startedAt;

    // 인증 서버에서
//    private String clientName;
//    private String clientCountry;
    private CurrencyCode sendCurrency;

    private String recipientCountry;
    private String recipientBankName;
    private String recipientAccountNo;
    private CurrencyCode receiveCurrency;
    private String recipientName;
    private String recipientPhoneCc;
    private String recipientPhoneNo;

    private RemittanceStatus remittanceStatus;
}
