package org.creditto.core_banking.domain.overseasremittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ExecuteRemittanceCommand {

    private String clientId;
    private Long recipientId;
    private Long accountId;
    private Long feeId;
    private Long regRemId; // 정기송금 ID (선택)
    private BigDecimal exchangeRate;
    private String currencyCode;
    private BigDecimal sendAmount;

    // 기존 DTO로부터 Command 객체를 쉽게 만들 수 있도록 정적 팩토리 메소드 추가
    public static ExecuteRemittanceCommand from(OverseasRemittanceRequestDto request) {
        return ExecuteRemittanceCommand.builder()
                .clientId(request.getClientId())
                .recipientId(request.getRecipientId())
                .accountId(request.getAccountId())
                .feeId(request.getFeeId())
                .regRemId(null) // RemittanceRequestDto에는 정기송금 ID가 없으므로 null로 설정
                .exchangeRate(request.getExchangeRate())
                .currencyCode(request.getCurrencyCode())
                .sendAmount(request.getSendAmount())
                .build();
    }
}
