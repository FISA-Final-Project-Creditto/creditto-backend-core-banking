package org.creditto.core_banking.domain.transaction.dto;

import org.creditto.core_banking.domain.account.dto.AccountRes;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnType;

import java.math.BigDecimal;

public record TransactionReq(
        Long accountId,
        BigDecimal txnAmount,
        TxnType txnType,
        Long typeId
) {

    public static TransactionReq from(Transaction transaction) {
        return new TransactionReq(
                transaction.getAccount().getId(),
                transaction.getTxnAmount(),
                transaction.getTxnType(),
                transaction.getTypeId()
        );
    }
}
