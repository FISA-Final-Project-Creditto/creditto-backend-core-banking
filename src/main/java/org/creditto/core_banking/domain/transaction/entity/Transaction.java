package org.creditto.core_banking.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.global.common.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(precision = 20, scale = 2) // => 정수18자리, 소수점 부분 2자리
    private BigDecimal txnAmount;

    @Enumerated(EnumType.STRING)
    private TxnType txnType;

    @Column(precision = 20, scale = 2) // => 정수18자리, 소수점 부분 2자리
    private BigDecimal balanceAfter;

    private LocalDateTime txnAt;

    public static Transaction of(Account account, BigDecimal txnAmount, TxnType txnType, BigDecimal balanceAfter, LocalDateTime txnAt) {
        return Transaction.builder()
                .account(account)
                .txnAmount(txnAmount)
                .txnType(txnType)
                .balanceAfter(balanceAfter)
                .txnAt(txnAt)
                .build();
    }
}
