package org.creditto.core_banking.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.transaction.dto.TransactionRes;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnResult;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<TransactionRes> findByAccountId(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountIdWithAccount(accountId);

        return transactions.stream()
                .map(TransactionRes::from)
                .toList();
    }

    /**
     * 거래 내역을 데이터베이스에 저장
     * 해당 메서드는 각 거래 전략에서 호출
     * 
     * @param account 거래가 발생한 계좌
     * @param amount 거래 금액
     * @param txnType 거래 유형
     * @param typeId 거래 관련 ID
     * @param txnResult 거래 결과
     * @return 저장된 거래 정보 DTO
     */

    // saveTransaction 메서드는 주 비즈니스 로직(계좌 입출금 등)의 트랜잭션과 분리되어야 함
    // 주 트랜잭션이 롤백되더라도, 거래 시도 자체는 로그로 남아야 하기 때문
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TransactionRes saveTransaction(Account account, BigDecimal amount, TxnType txnType, Long typeId, TxnResult txnResult) {
        // 새로운 Transaction 엔티티 생성
        Transaction transaction = Transaction.of(
                account,
                amount,
                txnType,
                typeId,
                txnResult
        );

        Transaction savedTransaction = transactionRepository.save(transaction);

        return TransactionRes.from(savedTransaction);
    }
}
