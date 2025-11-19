package org.creditto.core_banking.domain.overseasremittance.dto;

import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.entity.RemittanceStatus;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 해외송금 처리 결과 및 조회 결과를 클라이언트에게 반환하기 위한 DTO(Data Transfer Object)입니다.
 */
@Getter
@Builder
public class OverseasRemittanceResponseDto {

    /**
     * 송금 ID
     */
    private Long remittanceId;

    /**
     * 고객 ID
     */
    private String clientId;

    /**
     * 수취인 ID
     */
    private Long recipientId;

    /**
     * 수취인 이름
     */
    private String recipientName;

    /**
     * 출금 계좌 ID
     */
    private Long accountId;

    /**
     * 출금 계좌 번호
     */
    private String accountNo;

    /**
     * 정기송금 ID (일회성 송금의 경우 null)
     */
    private Long recurId;

    /**
     * 환전 ID
     */
    private Long exchangeId;

    /**
     * 적용 환율
     */
    private BigDecimal exchangeRate;

    /**
     * 수수료 내역 ID
     */
    private Long feeRecordId;

    /**
     * 송금 통화
     */
    private CurrencyCode sendCurrency;

    /**
     * 수취 통화
     */
    private CurrencyCode receiveCurrency;

    /**
     * 송금액
     */
    private BigDecimal sendAmount;

    /**
     * 최종 수취 금액
     */
    private BigDecimal receiveAmount;

    /**
     * 송금 시작일
     */
    private LocalDate startDate;

    /**
     * 송금 처리 상태
     */
    private RemittanceStatus remittanceStatus;

    /**
     * {@link OverseasRemittance} 엔티티로부터 {@link OverseasRemittanceResponseDto} 객체를 생성합니다.
     *
     * @param overseasRemittance 해외송금 엔티티
     * @return 생성된 DTO 객체
     */
    public static OverseasRemittanceResponseDto from(OverseasRemittance overseasRemittance) {
        return OverseasRemittanceResponseDto.builder()
                .remittanceId(overseasRemittance.getRemittanceId())
                .clientId(overseasRemittance.getClientId())
                .recipientId(overseasRemittance.getRecipient().getRecipientId())
                .recipientName(overseasRemittance.getRecipient().getName())
                .accountId(overseasRemittance.getAccount().getId())
                .accountNo(overseasRemittance.getAccount().getAccountNo())
                .recurId(overseasRemittance.getRecur() != null
                        ? overseasRemittance.getRecur().getRegRemId()
                        : null)
                .exchangeId(overseasRemittance.getExchange().getId())
                .exchangeRate(overseasRemittance.getExchange().getExchangeRate())
                .feeRecordId(overseasRemittance.getFeeRecord().getFeeRecordId())
                .sendCurrency(overseasRemittance.getSendCurrency())
                .receiveCurrency(overseasRemittance.getReceiveCurrency())
                .sendAmount(overseasRemittance.getSendAmount())
                .receiveAmount(overseasRemittance.getReceiveAmount())
                .startDate(overseasRemittance.getStartDate())
                .remittanceStatus(overseasRemittance.getRemittanceStatus())
                .build();
    }

}
