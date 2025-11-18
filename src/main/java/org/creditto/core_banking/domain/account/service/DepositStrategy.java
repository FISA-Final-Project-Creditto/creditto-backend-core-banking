package org.creditto.core_banking.domain.account.service;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.service.TransactionService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DepositStrategy extends AbstractTransactionStrategy {

    public DepositStrategy(TransactionService transactionService) {
        super(transactionService);
    }

    @Override
    protected void process(Account account, BigDecimal amount, Long typeId) {
        account.deposit(amount);
    }

    @Override
    public TxnType getTxnType() {
        return TxnType.DEPOSIT;
    }
}
