package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.overseasremittance.entity.RemittanceStatus;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RemittanceHistoryDetailDto {
    private String accountNo;
    private BigDecimal totalFee;
    private BigDecimal sendAmount;
    private String recipientBankName;
    private String recipientAccountNo;
    private RemittanceStatus remittanceStatus;
}
