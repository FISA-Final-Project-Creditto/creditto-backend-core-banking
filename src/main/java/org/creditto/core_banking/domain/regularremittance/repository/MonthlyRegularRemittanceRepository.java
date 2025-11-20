package org.creditto.core_banking.domain.regularremittance.repository;

import org.creditto.core_banking.domain.regularremittance.entity.MonthlyRegularRemittance;
import org.creditto.core_banking.domain.regularremittance.entity.RegRemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface MonthlyRegularRemittanceRepository extends JpaRepository<MonthlyRegularRemittance, Long> {

    @Query("SELECT m FROM MonthlyRegularRemittance m " +
            "JOIN FETCH m.account " +
            "JOIN FETCH m.recipient " +
            "WHERE m.scheduledDate IN :scheduledDates " +
            "AND m.regRemStatus = :regRemStatus"
    )
    Page<MonthlyRegularRemittance> findMonthlyRegularRemittanceByScheduledDateInAndRegRemStatus(
            @Param("scheduledDates") Collection<Integer> scheduledDates,
            @Param("regRemStatus") RegRemStatus regRemStatus,
            Pageable pageable);

    @Query("SELECT m FROM MonthlyRegularRemittance m " +
            "JOIN FETCH m.account " +
            "JOIN FETCH m.recipient " +
            "WHERE m.scheduledDate IN :scheduledDates " +
            "AND m.regRemStatus IN :regRemStatuses"
    )
    Page<MonthlyRegularRemittance> findMonthlyRegularRemittanceByScheduledDateInAndRegRemStatusIn(
            @Param("scheduledDates") Collection<Integer> scheduledDates,
            @Param("regRemStatuses") Collection<RegRemStatus> regRemStatuses,
            Pageable pageable
    );
}
