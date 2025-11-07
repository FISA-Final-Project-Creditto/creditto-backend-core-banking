package org.creditto.core_banking.domain.overseasremittance.repository;

import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OverseasRemittanceRepository extends JpaRepository<OverseasRemittance,Long> {

    // FETCH JOIN으로 N+1문제 방지
    @Query("SELECT r FROM OverseasRemittance r JOIN FETCH r.recipient JOIN FETCH r.account JOIN FETCH r.fee WHERE r.clientId = :clientId")
    List<OverseasRemittance> findByClientIdWithDetails(@Param("clientId") String clientId);
}
