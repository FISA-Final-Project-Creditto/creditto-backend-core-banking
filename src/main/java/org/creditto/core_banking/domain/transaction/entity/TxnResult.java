package org.creditto.core_banking.domain.transaction.entity;

import lombok.Getter;

@Getter
public enum TxnResult {

    SUCCESS("성공"),
    FAILURE("실패");

    private final String result;

    TxnResult(String result) {
        this.result = result;
    }
}
