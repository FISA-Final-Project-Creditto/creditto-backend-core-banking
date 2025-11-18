package org.creditto.core_banking.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.global.common.BaseEntity;

import java.math.BigDecimal;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(precision = 20, scale = 2)
    private BigDecimal txnAmount;

    @Enumerated(EnumType.STRING) // 거래 종류
    private TxnType txnType;

    private Long typeId; // 거래 종류 ID

    private TxnResult txnResult; // 거래 결과

    public static Transaction of(Account account, BigDecimal txnAmount, TxnType txnType, Long typeId, TxnResult txnResult) {
        return Transaction.builder()
                .account(account)
                .txnAmount(txnAmount)
                .txnType(txnType)
                .typeId(typeId)
                .txnResult(txnResult)
                .build();
    }
}
