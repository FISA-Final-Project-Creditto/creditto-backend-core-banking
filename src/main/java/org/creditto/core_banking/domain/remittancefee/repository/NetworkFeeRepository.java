package org.creditto.core_banking.domain.remittancefee.repository;

import org.creditto.core_banking.domain.remittancefee.entity.NetworkFee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NetworkFeeRepository extends JpaRepository<NetworkFee, Long> {
    Optional<NetworkFee> findByCurrencyCode(String currencyCode);
}
