package org.creditto.core_banking.domain.regularRemittance.repository;

import org.creditto.core_banking.domain.regularRemittance.entity.MonthlyRegularRemittance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyRegularRemittanceRepository extends JpaRepository<MonthlyRegularRemittance, Long> {
}
