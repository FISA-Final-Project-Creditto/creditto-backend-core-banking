package org.creditto.core_banking.domain.account.repository;

import org.creditto.core_banking.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 계좌 번호로 계좌 조회
    Optional<Account> findByAccountNo(String accountNo);

    // 클라이언트 ID로 계좌 조회
    List<Account> findAccountByUserId(Long userId);

    @Query("SELECT a.balance FROM Account a WHERE a.id = :id")
    Optional<BigDecimal> findBalanceById(@Param("id") Long id);

    @Query("SELECT COALESCE(SUM(ac.balance), 0) " +
            "FROM Account ac " +
            "WHERE ac.userId = :userId")
    BigDecimal sumAccountBalanceByUserId(@Param("userId") Long userId);
}
