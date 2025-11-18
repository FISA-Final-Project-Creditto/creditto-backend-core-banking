package org.creditto.core_banking.domain.transaction;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.transaction.dto.TransactionResponseDto;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.creditto.core_banking.domain.transaction.service.TransactionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TransactionImplTest {

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    TransactionServiceImpl transactionService;

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
                BigDecimal.valueOf(60000),
                LocalDateTime.now()
        );

        Transaction txn2 = Transaction.of(
                account,
                BigDecimal.valueOf(5000),
                TxnType.WITHDRAWAL,
                BigDecimal.valueOf(55000),
                LocalDateTime.now()
        );

        given(transactionRepository.findByAccountId(accountId))
                .willReturn(List.of(txn1, txn2));

        // when
        List<TransactionResponseDto> result = transactionService.findByAccountId(accountId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTxnType()).isEqualTo(TxnType.DEPOSIT);
        assertThat(result.get(1).getTxnAmount()).isEqualTo(BigDecimal.valueOf(5000));
        assertThat(result.get(1).getBalanceAfter()).isEqualTo(BigDecimal.valueOf(55000));
    }

    @Test
    @DisplayName("거래내역이 없을 경우 빈 리스트 반환")
    void findByAccountId_EmptyList() {
        // given
        Long accountId = 99L;
        given(transactionRepository.findByAccountId(accountId))
                .willReturn(List.of());

        // when
        List<TransactionResponseDto> result = transactionService.findByAccountId(accountId);

        // then
        assertThat(result).isEmpty();
    }
}
