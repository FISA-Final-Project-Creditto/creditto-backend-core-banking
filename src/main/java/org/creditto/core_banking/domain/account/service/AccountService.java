package org.creditto.core_banking.domain.account.service;

import org.creditto.core_banking.domain.account.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    BigDecimal getBalance(String accountId);

}
