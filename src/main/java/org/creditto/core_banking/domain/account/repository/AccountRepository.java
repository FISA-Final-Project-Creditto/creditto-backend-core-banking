package org.creditto.core_banking.domain.account.repository;

import org.creditto.core_banking.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 계좌id로 계좌 조회
    Optional<Account> findByAccountId(String accountId);

    // 계좌번호로 계좌 조회
    Optional<Account> findByAccountNo(String accountNo);

}
