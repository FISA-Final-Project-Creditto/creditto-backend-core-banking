package org.creditto.core_banking.domain.transaction.entity;

import lombok.Getter;

@Getter
public enum TxnType {

    DEPOSIT("입금"),
    WITHDRAWAL("출금"),
    FEE("수수료");

    private final String type;

    TxnType(String type) {
        this.type = type;
    }
}
