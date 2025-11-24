package org.creditto.core_banking.domain.regularremittance.repository;

import feign.Param;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegularRemittanceRepository extends JpaRepository<RegularRemittance,Long> {

    @Query("SELECT rr " +
            "FROM RegularRemittance rr " +
            "JOIN FETCH rr.account a " +
            "JOIN FETCH rr.recipient " +
            "WHERE a.externalUserId = :externalUserId")
    List<RegularRemittance> findByAccountExternalUserId(@Param("externalUserId") String externalUserId);
}
