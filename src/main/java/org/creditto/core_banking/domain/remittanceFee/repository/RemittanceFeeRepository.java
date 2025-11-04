package org.creditto.core_banking.domain.remittanceFee.repository;

import org.creditto.core_banking.domain.remittanceFee.entity.RemittanceFee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemittanceFeeRepository extends JpaRepository<RemittanceFee,Long> {
}
