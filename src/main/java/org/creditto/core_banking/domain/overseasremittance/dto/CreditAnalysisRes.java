package org.creditto.core_banking.domain.overseasremittance.dto;

import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.entity.RemittanceStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 신용도 분석을 위해 해외송금의 주요 정보를 제공하는 DTO 입니다.
 */
@Getter
@Builder
public class CreditAnalysisRes {

    private final Long userId;
    private final BigDecimal sendAmount;
    private final RemittanceStatus remittanceStatus;
    private final LocalDateTime createdAt;

    public static CreditAnalysisRes from(OverseasRemittance overseasRemittance) {
        return CreditAnalysisRes.builder()
                .userId(overseasRemittance.getUserId())
                .sendAmount(overseasRemittance.getSendAmount())
                .remittanceStatus(overseasRemittance.getRemittanceStatus())
                .createdAt(overseasRemittance.getCreatedAt())
                .build();
    }
}
