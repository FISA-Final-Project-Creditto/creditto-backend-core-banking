package org.creditto.core_banking.domain.account;

import org.creditto.core_banking.domain.account.dto.AccountRes;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.entity.AccountType;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.account.service.AccountService;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

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
        BigDecimal result = accountService.getAccountById(accountId).balance();

        // then
        assertThat(result).isEqualTo(BigDecimal.valueOf(100000));
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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage());
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
