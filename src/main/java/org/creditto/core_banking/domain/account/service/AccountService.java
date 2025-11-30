package org.creditto.core_banking.domain.account.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.dto.AccountCreateReq;
import org.creditto.core_banking.domain.account.dto.AccountRes;
import org.creditto.core_banking.domain.account.dto.AccountSummaryRes;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.account.service.strategy.TransactionStrategy;
import org.creditto.core_banking.domain.account.service.strategy.TransactionStrategyFactory;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionStrategyFactory strategyFactory;
    private final PasswordValidator passwordValidator;
    private final PasswordEncoder passwordEncoder;


    /**
     * 새로운 계좌를 생성
     * 계좌명, 계좌 종류, 클라이언트 ID, 초기 잔액을 받아 계좌를 생성하고 저장
     * 계좌 번호는 Account 엔티티의 generateAccountNo 메서드를 통해 자동으로 생성
     * 신규 계좌는 기본적으로 ACTIVE 상태로 생성
     * @param request 계좌 생성에 필요한 정보를 담은 AccountCreateReq DTO
     * @return 생성된 계좌 정보를 담은 AccountRes DTO
     */
    @Transactional
    public AccountRes createAccount(AccountCreateReq request, Long userId) {
        // 비밀번호 유효성 검사
        passwordValidator.validatePassword(request.password());

        // 비밀번호 일치 확인
        if (!request.password().equals(request.passwordConfirmation())) {
            throw new CustomBaseException(ErrorBaseCode.MISMATCH_PASSWORD);
        }

        // 비밀번호 인코딩
        String encodedPassword = passwordEncoder.encode(request.password());

        // accountNo는 Account 엔티티의 @PrePersist 메서드에서 생성
        Account account = Account.of(
                null, // accountNo는 @PrePersist에서 설정되므로 null로 전달
                encodedPassword,
                request.accountName(),
                BigDecimal.ZERO,
                request.accountType(),
                AccountState.ACTIVE,
                userId
        );

        Account savedAccount = accountRepository.save(account);
        return AccountRes.from(savedAccount);
    }

    // 비밀번호 검증
    public void verifyPassword(Long accountId, String rawPassword) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        String encodedPassword = account.getPassword();

        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new CustomBaseException(ErrorBaseCode.INVALID_PASSWORD);
        }
    }

    /**
     * 거래 유형(TxnType)에 따라 적절한 거래 전략 실행
     * @param accountId 대상 계좌 ID
     * @param amount amount 거래 금액
     * @param txnType txnType 거래 유형
     * @param typeId 거래 관련 ID
     */
    @Transactional
    public void processTransaction(Long accountId, BigDecimal amount, TxnType txnType, Long typeId) {
        // 팩토리에서 거래 유행에 맞는 전략 호출
        TransactionStrategy strategy = strategyFactory.getStrategy(txnType);

        // 계좌 정보 조회
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        // 거래 타입 실행
        strategy.execute(account, amount, typeId);
    }

    public AccountRes getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        return AccountRes.from(account);
    }

    public BigDecimal getAccountBalanceById(Long id) {
        return accountRepository.findBalanceById(id)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));
    }

    public AccountRes getAccountByAccountNo(String accountNo) {
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        return AccountRes.from(account);
    }

    public List<AccountRes> getAccountByUserId(Long userId) {
        List<Account> accounts = accountRepository.findAccountByUserId(userId);

        return accounts.stream()
                .map(AccountRes::from)
                .toList();
    }

    public AccountSummaryRes getTotalBalanceByUserId(Long userId) {
        return accountRepository.findAccountSummaryByUserId(userId);
    }
}
