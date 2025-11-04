package org.creditto.core_banking.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.global.common.BaseEntity;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    private String accountNo;

    private String accountName;

    @Column(precision = 20, scale = 2) // => 정수18자리, 소수점 부분 2자리
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    private AccountState accountState;

    private String clientId;

    public static Account of(String accountNo, String accountName, BigDecimal balance, AccountType accountType, AccountState accountState, String clientId) {
        return Account.builder()
                .accountNo(accountNo)
                .accountName(accountName)
                .balance(balance)
                .accountType(accountType)
                .accountState(accountState)
                .clientId(clientId)
                .build();
    }
}
