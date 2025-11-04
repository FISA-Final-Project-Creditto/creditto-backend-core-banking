package org.creditto.core_banking.domain.overseasRemittance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.regularRemittance.entity.RegularRemittance;
import org.creditto.core_banking.domain.remittanceFee.entity.RemittanceFee;
import org.creditto.core_banking.global.common.BaseEntity;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OverseasRemittance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long remittanceId;

    @ManyToOne
    private Recipient recipient;

    @ManyToOne
    private Account account;

    // clientId는 accountId를 통해 알 수 있지만
    // 조회 효율, 이력 데이터의 독립성 등을 고려하여 컬럼을 유지함
    private Long clientId;

    @OneToOne
    private RemittanceFee fee;

    @ManyToOne
    private RegularRemittance recur;

    private Double exchangeRate;

    private BigDecimal sendAmount;

    private BigDecimal receivedAmount;

    @Enumerated(EnumType.STRING)
    private RemittanceStatus remittanceStatus;

    public static OverseasRemittance of(
            Recipient recipient,
            Account account,
            Long clientId,
            RemittanceFee fee,
            RegularRemittance recur,
            Double exchangeRate,
            BigDecimal sendAmount,
            BigDecimal receivedAmount
    ){
        return OverseasRemittance.builder()
                .recipient(recipient)
                .account(account)
                .clientId(clientId)
                .fee(fee)
                .recur(recur)
                .exchangeRate(exchangeRate)
                .sendAmount(sendAmount)
                .receivedAmount(receivedAmount)
                .remittanceStatus(RemittanceStatus.PENDING)   //기본 상태 PENDING
                .build();
    }

}
