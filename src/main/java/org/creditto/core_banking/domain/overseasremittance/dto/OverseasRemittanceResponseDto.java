package org.creditto.core_banking.domain.overseasremittance.dto;

import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.entity.RemittanceStatus;

import java.math.BigDecimal;

@Getter
@Builder
public class OverseasRemittanceResponseDto {

    private Long remittanceId;          // 송금 ID
    private String recipientName;       // 수취인 이름
    private String accountNo;           // 출금 계좌 번호
    private String clientId;            // 클라이언트 ID
    private Long feeId;                 // 수수료 ID
    private Long regRemId;              // 정기송금 ID
    private BigDecimal exchangeRate;    // 환율
    private BigDecimal sendAmount;      // 송금 금액
    private BigDecimal receivedAmount;  // 수취 금액
    private RemittanceStatus remittanceStatus;  // 처리상태

    public static OverseasRemittanceResponseDto from(OverseasRemittance remittance) {
        return OverseasRemittanceResponseDto.builder()
                .remittanceId(remittance.getRemittanceId())
                .recipientName(remittance.getRecipient().getName())
                .accountNo(remittance.getAccount().getAccountNo())
                .clientId(remittance.getClientId())
                .feeId(remittance.getFee().getFeeId())
                .regRemId(remittance.getRecur() != null
                        ? remittance.getRecur().getRegRemId()
                        : null)        // 정기송금이 아닌경우 null 반환
                .exchangeRate(remittance.getExchangeRate())
                .sendAmount(remittance.getSendAmount())
                .receivedAmount(remittance.getReceivedAmount())
                .remittanceStatus(remittance.getRemittanceStatus())
                .build();
    }

}

