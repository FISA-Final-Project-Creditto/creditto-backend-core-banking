package org.creditto.core_banking.domain.overseasremittance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.exchange.entity.Exchange;
import org.creditto.core_banking.domain.overseasremittance.dto.ExecuteRemittanceCommand;
import org.creditto.core_banking.domain.remittancefee.entity.FeeRecord;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.creditto.core_banking.global.common.BaseEntity;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 해외송금 거래 정보를 나타내는 엔티티입니다.
 * 한 건의 해외송금은 고객, 계좌, 수취인, 수수료, 환율 등 다양한 정보를 포함합니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OverseasRemittance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long remittanceId;

    /**
     * 수취인 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    /**
     * 출금 계좌 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /**
     * 정기송금 정보 (일회성 송금의 경우 null)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recur_id")
    private RegularRemittance recur;

    /**
     * 적용된 환전 내역
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;

    /**
     * 적용된 수수료 내역
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_record_id", nullable = false)
    private FeeRecord feeRecord;

    /**
     * 송금을 요청한 고객의 ID
     */
    private String clientId;

    /**
     * 송금 통화
     */
    @Enumerated(EnumType.STRING)
    private CurrencyCode sendCurrency;

    /**
     * 수취 통화
     */
    @Enumerated(EnumType.STRING)
    private CurrencyCode receiveCurrency;

    /**
     * 송금액 (원화)
     */
    private BigDecimal sendAmount;

    /**
     * 최종 수취 금액 (외화)
     */
    private BigDecimal receiveAmount;

    /**
     * 송금 시작일
     */
    private LocalDate startDate;

    /**
     * 송금 처리 상태
     */
    @Enumerated(EnumType.STRING)
    private RemittanceStatus remittanceStatus;

    /**
     * OverseasRemittance 엔티티를 생성하는 정적 팩토리 메서드입니다.
     * 초기 송금 상태는 PENDING으로 설정됩니다.
     *
     * @param recipient       수취인 엔티티
     * @param account         출금 계좌 엔티티
     * @param recur           정기송금 엔티티 (선택 사항)
     * @param exchange        적용 환전 내역
     * @param feeRecord       적용된 수수료 내역
     * @param sendAmount      송금액
     * @param command         해외송금 실행에 필요한 모든 데이터
     * @return 새로운 OverseasRemittance 객체
     */
    public static OverseasRemittance of(
            Recipient recipient,
            Account account,
            RegularRemittance recur,
            Exchange exchange,
            FeeRecord feeRecord,
            BigDecimal sendAmount,
            ExecuteRemittanceCommand command
    ){
        return OverseasRemittance.builder()
                .recipient(recipient)
                .account(account)
                .recur(recur)
                .exchange(exchange)
                .feeRecord(feeRecord)
                .clientId(command.clientId())
                .sendCurrency(command.sendCurrency())
                .receiveCurrency(command.receiveCurrency())
                .sendAmount(sendAmount)
                .receiveAmount(command.targetAmount())
                .startDate(command.startDate())
                .remittanceStatus(RemittanceStatus.PENDING)   //기본 상태 PENDING
                .build();
    }

}