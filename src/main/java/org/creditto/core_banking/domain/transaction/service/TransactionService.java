package org.creditto.core_banking.domain.transaction.service;

import org.creditto.core_banking.domain.transaction.entity.Transaction;

import java.util.List;

public interface TransactionService {

    List<Transaction> findByAccountId(Long accountId);
}
