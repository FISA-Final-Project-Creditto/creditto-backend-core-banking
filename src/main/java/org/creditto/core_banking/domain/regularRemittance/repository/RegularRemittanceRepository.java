package org.creditto.core_banking.domain.regularRemittance.repository;

import org.creditto.core_banking.domain.regularRemittance.entity.RegularRemittance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegularRemittanceRepository extends JpaRepository<RegularRemittance,Long> {
}
