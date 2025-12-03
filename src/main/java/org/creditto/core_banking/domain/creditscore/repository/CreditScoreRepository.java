package org.creditto.core_banking.domain.creditscore.repository;

import org.creditto.core_banking.domain.creditscore.entity.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditScoreRepository extends JpaRepository<CreditScore, Long> {

    Optional<CreditScore> findByUserId(Long userId);
}
