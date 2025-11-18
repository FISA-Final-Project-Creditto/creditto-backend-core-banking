package org.creditto.core_banking.domain.account.service;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.transaction.entity.TxnType;

import java.math.BigDecimal;

public interface TransactionStrategy {

    // 어떤 거래 타입에 해당하는지 알려주는 메서드
    TxnType getTxnType();

    // 실제 계좌 처리 로직을 실행하는 메서드
    void execute(Account account, BigDecimal amount, Long typeId);
}
