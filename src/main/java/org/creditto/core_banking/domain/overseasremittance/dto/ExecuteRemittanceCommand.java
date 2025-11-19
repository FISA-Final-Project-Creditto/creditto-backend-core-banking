package org.creditto.core_banking.domain.overseasremittance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 서비스 계층 내부에서 해외송금 실행에 필요한 모든 데이터를 전달하는 불변 Command 객체입니다.
 * CQRS 패턴의 Command에 해당하며, 송금 처리에 필요한 모든 정보를 포함합니다.
 * 이 객체는 순수한 데이터 컨테이너이며, 생성 로직은 Application Service 계층에서 처리됩니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class ExecuteRemittanceCommand {

    /**
     * 고객 식별자
     */
    private String clientId;

    /**
     * 수취인 엔티티의 식별자
     */
    private Long recipientId;

    /**
     * 출금계좌 엔티티의 식별자
     */
    private Long accountId;

    /**
     * 정기송금 엔티티의 식별자 (일회성 송금의 경우 null)
     */
    private Long regRemId;

    /**
     * 보내는 통화 (e.g., "KRW")
     */
    private CurrencyCode sendCurrency;

    /**
     * 받는 통화 (e.g., "USD")
     */
    private CurrencyCode receiveCurrency;

    /**
     * 보내는 금액 (수취 통화 기준)
     */
    private BigDecimal targetAmount;

    /**
     * 송금 시작일 (정기송금의 경우 사용)
     */
    private LocalDate startDate;

}
