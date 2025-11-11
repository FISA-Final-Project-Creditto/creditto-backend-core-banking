package org.creditto.core_banking.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;

import java.math.BigDecimal;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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


    // 입금
    public void deposit(BigDecimal amount) {
        this.balance = balance.add(amount);
    }

    // 출금
    public void withdraw(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("계좌 잔액이 부족합니다.");
        }

        this.balance = balance.subtract(amount);
    }

}
