package org.creditto.core_banking.domain.account.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.dto.AccountRes;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountRes getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage()));

        return AccountRes.from(account);
    }

    public AccountRes getAccountByAccountNo(String accountNo) {
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new IllegalArgumentException(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage()));

        return AccountRes.from(account);
    }

    public List<AccountRes> getAccountByClientId(String clientId) {
        List<Account> accounts = accountRepository.findByClientId(clientId);

        return accounts.stream()
                .map(AccountRes::from)
                .toList();
    }
}
