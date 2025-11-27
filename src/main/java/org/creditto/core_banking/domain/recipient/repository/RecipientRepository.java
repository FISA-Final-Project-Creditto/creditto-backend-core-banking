package org.creditto.core_banking.domain.recipient.repository;

import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipientRepository extends JpaRepository<Recipient, Long> {
    // 은행 코드, 계좌 번호, 이름을 기준으로 수취인을 조회
    Optional<Recipient> findByBankCodeAndAccountNoAndName(String bankCode, String accountNo, String name);

    // 계좌 번호로 수취인을 조회
    Optional<Recipient> findByAccountNo(String accountNo);
}
