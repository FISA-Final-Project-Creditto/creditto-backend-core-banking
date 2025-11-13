package org.creditto.core_banking.domain.overseasremittance.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.creditto.core_banking.domain.remittancefee.entity.RemittanceFee;
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

    @ManyToOne(fetch = FetchType.LAZY)
    private Recipient recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private String clientId;

    @OneToOne(fetch = FetchType.LAZY)
    private RemittanceFee fee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recur_id")
    private RegularRemittance recur;

    private BigDecimal exchangeRate;

    private BigDecimal sendAmount;

    private BigDecimal receivedAmount;

    @Enumerated(EnumType.STRING)
    private RemittanceStatus remittanceStatus;

    public static OverseasRemittance of(
            Recipient recipient,
            Account account,
            String clientId,
            RemittanceFee fee,
            RegularRemittance recur,
            BigDecimal exchangeRate,
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