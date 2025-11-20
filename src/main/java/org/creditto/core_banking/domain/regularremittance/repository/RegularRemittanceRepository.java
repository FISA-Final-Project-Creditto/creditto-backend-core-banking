package org.creditto.core_banking.domain.regularremittance.repository;

import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegularRemittanceRepository extends JpaRepository<RegularRemittance,Long> {
    List<RegularRemittance> findAllByAccount_ExternalUserId(String externalUserId);
}
