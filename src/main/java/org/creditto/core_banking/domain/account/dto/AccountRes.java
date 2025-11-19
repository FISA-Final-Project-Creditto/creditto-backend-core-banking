package org.creditto.core_banking.domain.account.dto;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.entity.AccountType;

import java.math.BigDecimal;

public record AccountRes(
    String accountNo,
    String accountName,
    BigDecimal balance,
    AccountType accountType,
    AccountState accountState,
    String clientId
) {
    public static AccountRes from(Account account) {
        return new AccountRes(
                account.getAccountNo(),
                account.getAccountName(),
                account.getBalance(),
                account.getAccountType(),
                account.getAccountState(),
                account.getExternalUserId()
        );
    }
}
