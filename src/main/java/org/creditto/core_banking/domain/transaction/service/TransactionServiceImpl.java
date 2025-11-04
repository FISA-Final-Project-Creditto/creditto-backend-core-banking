package org.creditto.core_banking.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.transaction.dto.TransactionResponseDto;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public List<TransactionResponseDto> findByAccountId(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        return transactions.stream()
                .map(TransactionResponseDto::from)
                .toList();
    }
}
