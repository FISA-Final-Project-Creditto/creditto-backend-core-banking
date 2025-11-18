package org.creditto.core_banking.domain.account.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.transaction.entity.TxnResult;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.service.TransactionService;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.creditto.core_banking.global.response.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public abstract class AbstractTransactionStrategy implements TransactionStrategy {

    protected final TransactionService transactionService;

    @Transactional
    public final void execute(Account account, BigDecimal amount, Long typeId) {
        try {
            // 실제 비즈니스 로직 처리
            process(account, amount, typeId);

            // 성공 트랜잭션 생성 및 저장
            transactionService.saveTransaction(account, amount, getTxnType(), typeId, TxnResult.SUCCESS);

        } catch (CustomException e) {
            // 실패 트랜잭션 저장 후
            saveFailedTransaction(account, amount, typeId);
            throw new CustomBaseException(ErrorBaseCode.TRANSACTION_FAILED);
        } catch (Exception e) {
            saveFailedTransaction(account, amount, typeId);
            throw new IllegalArgumentException(ErrorBaseCode.TRANSACTION_FAILED.getMessage());
        }
    }

    private void saveFailedTransaction(Account account, BigDecimal amount, Long typeId) {
        transactionService.saveTransaction(account, amount, getTxnType(), typeId, TxnResult.FAILURE);
    }

    /**
     * 자식 클래스에서 실제 비즈니스 로직을 구현해야 하는 추상 메서드
     */
    protected abstract void process(Account account, BigDecimal amount, Long typeId);

    /**
     * 자식 클래스에서 자신의 트랜잭션 타입을 반환해야 하는 추상 메서드
     */
    public abstract TxnType getTxnType();
}

