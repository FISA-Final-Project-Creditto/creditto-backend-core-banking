package org.creditto.core_banking.domain.account.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.EnumMap;
import java.util.Map;

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

    private static final Map<AccountType, String> ACCOUNT_TYPES_SETTING = new EnumMap<>(Map.of(
            AccountType.DEPOSIT, "1002",
            AccountType.SAVINGS, "181",
            AccountType.LOAN, "207",
            AccountType.INVESTMENT, "520"
    ));

    private static final int ACCOUNT_NO_LENGTH = 13;
    private static final SecureRandom RANDOM = new SecureRandom();

    @PrePersist
    protected void prePersist() {
        generateAccountNo();
    }

    // 계좌 번호 생성
    private void generateAccountNo() {
        if (this.accountNo != null) {
            return; // 이미 계좌 번호가 있으면 다시 생성하지 않음
        }
        String prefix = ACCOUNT_TYPES_SETTING.get(this.accountType);
        if (prefix == null) {
            throw new CustomBaseException(ErrorBaseCode.INVALID_ACCOUNT_TYPE);
        }

        StringBuilder sb = new StringBuilder(prefix);
        int length = ACCOUNT_NO_LENGTH - prefix.length();
        for (int i = 0; i < length; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        this.accountNo = sb.toString();
    }


    // 입금
    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    // 출금
    public void withdraw(BigDecimal amount) {
        if (!this.checkSufficientBalance(amount)) {
            throw new CustomBaseException(ErrorBaseCode.INSUFFICIENT_FUNDS);
        }

        this.balance = balance.subtract(amount);
    }

    // 출금 가능한지 확인
    public boolean checkSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }

}