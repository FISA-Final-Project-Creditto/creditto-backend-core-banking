package org.creditto.core_banking.domain.account;

import org.creditto.core_banking.domain.account.dto.AccountCreateReq;
import org.creditto.core_banking.domain.account.dto.AccountRes;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.entity.AccountType;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.account.service.AccountService;
import org.creditto.core_banking.domain.account.service.TransactionStrategy;
import org.creditto.core_banking.domain.account.service.TransactionStrategyFactory;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionStrategyFactory strategyFactory; // 의존성 추가

    @Mock
    private TransactionStrategy mockStrategy; // 테스트에 사용할 가짜 전략

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("계좌 생성 성공")
    void createAccount_Success() {
        // Given
        AccountCreateReq request = new AccountCreateReq(
                "새로운 계좌",
                AccountType.DEPOSIT
        );

        String externalUserId = "externalUserId";

        // accountNo는 @PrePersist를 통해 엔티티 내부에서 생성되므로,
        // 테스트에서는 accountRepository.save()가 반환할 Account 객체를 미리 정의하여 모킹합니다.
        String expectedAccountNo = "MOCKED_ACCOUNT_NO"; // 테스트를 위한 가상의 계좌 번호
        Account mockSavedAccount = Account.of(
                expectedAccountNo, // @PrePersist에 의해 생성될 것으로 예상되는 계좌 번호
                request.accountName(),
                BigDecimal.ZERO,
                request.accountType(),
                AccountState.ACTIVE,
                externalUserId
        );
        // ID는 save 시점에 부여된다고 가정
        given(accountRepository.save(any(Account.class))).willReturn(mockSavedAccount);

        // When
        AccountRes result = accountService.createAccount(request, externalUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accountNo()).isEqualTo(expectedAccountNo);
        assertThat(result.accountName()).isEqualTo(request.accountName());
        assertThat(result.accountType()).isEqualTo(request.accountType());
        assertThat(result.accountState()).isEqualTo(AccountState.ACTIVE);
        assertThat(result.clientId()).isEqualTo(externalUserId);

    }

    @Test
    @DisplayName("거래 처리 로직(processTransaction) 성공")
    void processTransaction_Success() {
        // given
        Long accountId = 1L;
        BigDecimal amount = new BigDecimal("10000");
        TxnType txnType = TxnType.WITHDRAWAL;
        Long relatedId = null;

        Account mockAccount = Account.of(
                "ACC001",
                "테스트 계좌",
                BigDecimal.valueOf(50000),
                AccountType.DEPOSIT,
                AccountState.ACTIVE,
                "CLIENT001"
        );

        // 1. accountRepository.findById가 호출되면 mockAccount를 반환
        given(accountRepository.findById(accountId)).willReturn(Optional.of(mockAccount));
        // 2. strategyFactory.getStrategy가 호출되면 가짜 전략(mockStrategy)을 반환
        given(strategyFactory.getStrategy(txnType)).willReturn(mockStrategy);

        // when
        accountService.processTransaction(accountId, amount, txnType, relatedId);

        // then
        // 1. accountRepository.findById가 정확한 인자로 1번 호출되었는지 검증
        verify(accountRepository).findById(accountId);
        // 2. strategyFactory.getStrategy가 정확한 인자로 1번 호출되었는지 검증
        verify(strategyFactory).getStrategy(txnType);
        // 3. mockStrategy.execute가 정확한 인자들로 1번 호출되었는지 검증
        verify(mockStrategy).execute(mockAccount, amount, relatedId);
    }

    @Test
    @DisplayName("잔액 조회 성공")
    void getBalance_ById_Success() {
        // given
        Long accountId = 1L;
        String accountNo = "ACC001";
        String clientId = "CLIENT001";
        Account mockAccount = Account.of(
                accountNo,
                "테스트 계좌",
                BigDecimal.valueOf(100000),
                AccountType.DEPOSIT,
                AccountState.ACTIVE,
                clientId
        );

        given(accountRepository.findById(accountId))
                .willReturn(Optional.of(mockAccount));

        // when
        AccountRes result = accountService.getAccountById(accountId);

        // then
        assertThat(result.balance()).isEqualByComparingTo(BigDecimal.valueOf(100000));
    }

    @Test
    @DisplayName("존재하지 않는 계좌 번호로 조회 시 예외 발생")
    void getBalance_ById_AccountNotFound() {
        // given
        String invalidAccountNo = "ACC999";
        given(accountRepository.findByAccountNo(invalidAccountNo))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.getAccountByAccountNo(invalidAccountNo))
                .isInstanceOf(CustomBaseException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorBaseCode.NOT_FOUND_ACCOUNT);
    }


    @Test
    @DisplayName("클라이언트 ID로 계좌 조회 성공")
    void getAccountByClientId_Success() {
        // given
        String clientId = "CLIENT001";
        
        Account account1 = Account.of(
                "ACC001",
                "테스트 계좌",
                BigDecimal.valueOf(100000),
                AccountType.DEPOSIT,
                AccountState.ACTIVE,
                clientId
        );

        Account account2 = Account.of(
                "ACC002",
                "적금계좌",
                BigDecimal.valueOf(50000),
                AccountType.SAVINGS,
                AccountState.ACTIVE,
                clientId
        );

        
        given(accountRepository.findByClientId(clientId))
                .willReturn(List.of(account1, account2));
        
        // when
        List<AccountRes> result = accountService.getAccountByClientId(clientId);

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).accountNo()).isEqualTo("ACC001");
        assertThat(result.get(1).accountName()).isEqualTo("적금계좌");
    }
}
