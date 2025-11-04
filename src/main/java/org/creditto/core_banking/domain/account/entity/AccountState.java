package org.creditto.core_banking.domain.account.entity;

import lombok.Getter;

@Getter
public enum AccountState {

    ACTIVE("정상"),
    DORMANT("휴면"),
    SUSPENDED("정지"),
    CLOSED("해지"),
    PENDING("대기");

    private final String state;

    AccountState(String state) {
        this.state = state;
    }
}
