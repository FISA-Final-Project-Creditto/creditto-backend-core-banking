package org.creditto.core_banking.domain.transaction.dto;

import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRes (
        Long accountId,
        BigDecimal txnAmount,
        TxnType txnType,
        Long typeId,
        LocalDateTime txnTime
){

    public static TransactionRes from(Transaction transaction) {
        return new TransactionRes (
                transaction.getAccount().getId(),
                transaction.getTxnAmount(),
                transaction.getTxnType(),
                transaction.getTypeId(),
                transaction.getCreatedAt()
        );
    }
}
