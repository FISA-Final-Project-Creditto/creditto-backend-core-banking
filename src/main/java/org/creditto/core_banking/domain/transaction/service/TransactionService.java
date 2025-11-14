package org.creditto.core_banking.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.transaction.dto.TransactionReq;
import org.creditto.core_banking.domain.transaction.dto.TransactionRes;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<TransactionRes> findByAccountId(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        return transactions.stream()
                  .map(TransactionRes::from)
                .toList();
    }

    @Transactional
    public TransactionRes saveTransaction(TransactionReq req) {

        Account account = accountRepository.findById(req.accountId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        Transaction transaction = Transaction.of(
                account,
                req.txnAmount(),
                req.txnType(),
                req.typeId()
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionRes.from(savedTransaction);
    }
}
