package org.creditto.core_banking.domain.transaction;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.entity.AccountType;
import org.creditto.core_banking.domain.transaction.dto.TransactionRes;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnResult;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.creditto.core_banking.domain.transaction.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("계좌 ID로 거래 내역 조회 성공")
    void findByAccountId_Success() {
        // given
        Long accountId = 1L;
        Account mockAccount = mock(Account.class);
        given(mockAccount.getId()).willReturn(accountId);

        Transaction mockTransaction = mock(Transaction.class);
        given(mockTransaction.getId()).willReturn(99L);
        given(mockTransaction.getAccount()).willReturn(mockAccount);
        given(mockTransaction.getTxnAmount()).willReturn(new BigDecimal("1000"));
        given(mockTransaction.getTxnType()).willReturn(TxnType.DEPOSIT);
        given(mockTransaction.getTypeId()).willReturn(1L);
        given(mockTransaction.getCreatedAt()).willReturn(java.time.LocalDateTime.now());

        given(transactionRepository.findByAccountIdWithAccount(accountId)).willReturn(List.of(mockTransaction));

        // when
        List<TransactionRes> result = transactionService.findByAccountId(accountId);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        TransactionRes res = result.get(0);
        assertThat(res.txnId()).isEqualTo(99L);
        assertThat(res.accountId()).isEqualTo(accountId);
        assertThat(res.txnAmount()).isEqualByComparingTo("1000");
        assertThat(res.txnType()).isEqualTo(TxnType.DEPOSIT);
    }

    @Test
    @DisplayName("새로운 거래 기록 저장 성공")
    void saveTransaction_Success() {
        // given
        // 1. Account.of()를 사용하여 테스트용 Account 객체 생성
        Account mockAccount = Account.of(
                "123-456",
                "테스트 계좌",
                "password",
                BigDecimal.ZERO,
                AccountType.SAVINGS,
                AccountState.ACTIVE,
                1L
        );

        BigDecimal amount = new BigDecimal("50000");
        TxnType txnType = TxnType.DEPOSIT;
        TxnResult txnResult = TxnResult.SUCCESS;
        Long typeId = 101L;

        // 2. repository.save()가 반환할 Transaction 객체를 Transaction.of()로 생성
        Transaction savedTransaction = Transaction.of(mockAccount, amount, txnType, typeId, txnResult);

        // 3. Mockito 설정: repository.save()가 호출되면 위에서 만든 savedTransaction 객체를 반환하도록 설정
        given(transactionRepository.save(any(Transaction.class))).willReturn(savedTransaction);

        // when
        // 4. 실제 서비스 메서드 호출. 반환 타입은 TransactionRes DTO.
        TransactionRes resultDto = transactionService.saveTransaction(mockAccount, amount, txnType, typeId, txnResult);

        // then
        // 5. 반환된 DTO가 null이 아닌지, 내용이 올바른지 검증
        assertThat(resultDto).isNotNull();
        assertThat(resultDto.txnAmount()).isEqualByComparingTo(amount);
        assertThat(resultDto.txnType()).isEqualTo(txnType);

        // 6. repository.save()에 전달된 Transaction 객체를 캡처하여 내용 검증
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction capturedTxn = captor.getValue();

        assertThat(capturedTxn.getAccount()).isEqualTo(mockAccount);
        assertThat(capturedTxn.getTxnAmount()).isEqualByComparingTo(amount);
        assertThat(capturedTxn.getTxnType()).isEqualTo(txnType);
        assertThat(capturedTxn.getTxnResult()).isEqualTo(txnResult);
        assertThat(capturedTxn.getTypeId()).isEqualTo(typeId);
    }
}
