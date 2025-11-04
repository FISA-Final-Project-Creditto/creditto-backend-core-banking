package org.creditto.core_banking.domain.account.entity;

import lombok.Getter;

@Getter
public enum AccountType {

    DEPOSIT("입출금 계좌"),
    SAVINGS("적금 계좌"),
    LOAN("대출 계좌"),
    INVESTMENT("투자 계좌");

    private final String type;

    AccountType(String type) {
        this.type = type;
    }
}
