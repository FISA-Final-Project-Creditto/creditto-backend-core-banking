package org.creditto.core_banking.domain.account.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.dto.AccountRes;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionStrategyFactory strategyFactory;

    /**
     * 거래 유형(TxnType)에 따라 적절한 거래 전략 실행
     * @param accountId 대상 계좌 ID
     * @param amount amount 거래 금액
     * @param txnType txnType 거래 유형
     * @param typeId 거래 관련 ID
     */
    @Transactional
    public void processTransaction(Long accountId, BigDecimal amount, TxnType txnType, Long typeId) {
        // 1. 팩토리에서 거래 유행에 맞는 전략 호출
        TransactionStrategy strategy = strategyFactory.getStrategy(txnType);

        // 2. 계좌 정보 조회
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        // 3. 거래 타입 실행
        strategy.execute(account, amount, typeId);
    }

    public AccountRes getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        return AccountRes.from(account);
    }

    public AccountRes getAccountByAccountNo(String accountNo) {
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        return AccountRes.from(account);
    }

    public List<AccountRes> getAccountByClientId(String clientId) {
        List<Account> accounts = accountRepository.findByClientId(clientId);

        return accounts.stream()
                .map(AccountRes::from)
                .toList();
    }
}
