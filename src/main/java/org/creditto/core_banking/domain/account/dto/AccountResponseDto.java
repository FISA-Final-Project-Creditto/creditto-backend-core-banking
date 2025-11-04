package org.creditto.core_banking.domain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.entity.AccountType;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {

    private String accountNo;
    private String accountName;
    private BigDecimal balance;
    private AccountType accountType;
    private AccountState accountState;
    private String clientId;

    public static AccountResponseDto from(Account account) {
        return AccountResponseDto.builder()
                .accountNo(account.getAccountNo())
                .accountName(account.getAccountName())
                .balance(account.getBalance())
                .accountType(account.getAccountType())
                .accountState(account.getAccountState())
                .clientId(account.getClientId())
                .build();
    }
}
