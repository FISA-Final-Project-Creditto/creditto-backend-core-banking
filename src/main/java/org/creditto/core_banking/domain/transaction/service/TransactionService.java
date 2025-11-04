package org.creditto.core_banking.domain.transaction.service;

import org.creditto.core_banking.domain.transaction.dto.TransactionResponseDto;

import java.util.List;

public interface TransactionService {

    List<TransactionResponseDto> findByAccountId(Long accountId);
}
