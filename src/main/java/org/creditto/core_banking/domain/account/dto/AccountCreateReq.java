package org.creditto.core_banking.domain.account.dto;

import org.creditto.core_banking.domain.account.entity.AccountType;


public record AccountCreateReq(
        String accountName,
        AccountType accountType
) {
}