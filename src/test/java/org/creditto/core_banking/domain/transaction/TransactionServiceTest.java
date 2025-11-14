package org.creditto.core_banking.domain.transaction;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.transaction.dto.TransactionReq;
import org.creditto.core_banking.domain.transaction.dto.TransactionRes;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.creditto.core_banking.domain.transaction.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("계좌 ID로 거래내역 조회 성공")
    void findByAccountId_Success() {
        // given
        Long accountId = 1L;
        Account account = Account.of(
                "ACC001",
                "테스트 계좌",
                BigDecimal.valueOf(50000),
                null, null, "CLIENT001"
        );

        Transaction txn1 = Transaction.of(
                account,
                BigDecimal.valueOf(10000),
                TxnType.DEPOSIT,
                1L
        );

        Transaction txn2 = Transaction.of(
                account,
                BigDecimal.valueOf(5000),
                TxnType.WITHDRAWAL,
                2L
        );

        given(transactionRepository.findByAccountId(accountId))
                .willReturn(List.of(txn1, txn2));

        // when
        List<TransactionRes> result = transactionService.findByAccountId(accountId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).txnType()).isEqualTo(TxnType.DEPOSIT);
        assertThat(result.get(1).txnAmount()).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("거래내역이 없을 경우 빈 리스트 반환")
    void findByAccountId_EmptyList() {
        // given
        Long accountId = 99L;
        given(transactionRepository.findByAccountId(accountId))
                .willReturn(List.of());

        // when
        List<TransactionRes> result = transactionService.findByAccountId(accountId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("거래 저장 성공")
    void saveTransaction_Success() {
        // given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("1000");
        TransactionReq request = new TransactionReq(accountId, amount, TxnType.DEPOSIT, 1L);

        Account account = Account.of(
                "ACC001",
                "테스트 계좌",
                BigDecimal.valueOf(50000),
                null, null, "CLIENT001"
        );

        Transaction transaction = Transaction.of(
                account,
                amount,
                TxnType.DEPOSIT,
                1L
        );

        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(transactionRepository.save(any(Transaction.class))).willReturn(transaction);

        // when
        TransactionRes response = transactionService.saveTransaction(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.txnAmount()).isEqualByComparingTo(amount);
        assertThat(response.txnType()).isEqualTo(TxnType.DEPOSIT);
        verify(transactionRepository).save(any(Transaction.class));
    }
}
