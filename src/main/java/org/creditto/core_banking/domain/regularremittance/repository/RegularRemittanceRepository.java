package org.creditto.core_banking.domain.regularremittance.repository;

import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegularRemittanceRepository extends JpaRepository<RegularRemittance,Long> {

    @Query("SELECT rr " +
            "FROM RegularRemittance rr " +
            "JOIN FETCH rr.account a " +
            "JOIN FETCH rr.recipient " +
            "WHERE a.userId = :userId")
    List<RegularRemittance> findByAccountUserId(@Param("userId") Long userId);
}
