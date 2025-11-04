package org.creditto.core_banking.domain.regularRemittance.repository;

import org.creditto.core_banking.domain.regularRemittance.entity.WeeklyRegularRemittance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyRegularRemittanceRepository extends JpaRepository<WeeklyRegularRemittance, Long> {
}
