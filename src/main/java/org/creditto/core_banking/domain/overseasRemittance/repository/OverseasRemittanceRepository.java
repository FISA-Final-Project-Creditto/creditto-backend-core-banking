package org.creditto.core_banking.domain.overseasRemittance.repository;

import org.creditto.core_banking.domain.overseasRemittance.entity.OverseasRemittance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverseasRemittanceRepository extends JpaRepository<OverseasRemittance,Long> {
}
