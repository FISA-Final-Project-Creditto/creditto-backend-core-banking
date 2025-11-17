package org.creditto.core_banking.domain.account.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.transaction.entity.TxnResult;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.service.TransactionService;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class FeeStrategy implements TransactionStrategy {

    private final TransactionService transactionService;

    @Override
    public TxnType getTxnType() {
        return TxnType.FEE;
    }

    @Override
    public void execute(Account account, BigDecimal amount, Long typeId) {

        try {
            TxnResult result = TxnResult.SUCCESS;
            account.withdraw(amount);
            transactionService.saveTransaction(account, amount, TxnType.FEE, typeId, result);

        } catch (CustomBaseException e) {
            TxnResult result = TxnResult.FAILURE;
            transactionService.saveTransaction(account, amount, TxnType.FEE, typeId, result);
            throw new CustomBaseException(ErrorBaseCode.TRANSACTION_FAILED);
        }
    }
}
