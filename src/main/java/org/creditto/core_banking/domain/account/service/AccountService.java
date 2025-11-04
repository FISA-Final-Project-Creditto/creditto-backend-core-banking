package org.creditto.core_banking.domain.account.service;


import org.creditto.core_banking.domain.account.dto.AccountResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    BigDecimal getBalance(Long id);

    AccountResponseDto findByAccountNo(String accountNo);

    List<AccountResponseDto> findByClientId(String clientId);
}
