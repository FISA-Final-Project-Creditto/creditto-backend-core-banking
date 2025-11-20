package org.creditto.core_banking.domain.regularremittance.repository;

import org.creditto.core_banking.domain.regularremittance.entity.RegRemStatus;
import org.creditto.core_banking.domain.regularremittance.entity.WeeklyRegularRemittance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;

@Repository
public interface WeeklyRegularRemittanceRepository extends JpaRepository<WeeklyRegularRemittance, Long> {

    @Query("SELECT w FROM WeeklyRegularRemittance w " +
            "JOIN FETCH w.account " +
            "JOIN FETCH w.recipient " +
            "WHERE w.scheduledDay = :scheduledDay " +
            "AND w.regRemStatus = :regRemStatus")
    Page<WeeklyRegularRemittance> findWeeklyRegularRemittanceByScheduledDayAndRegRemStatus(
            @Param("scheduledDay") DayOfWeek scheduledDay,
            @Param("regRemStatus") RegRemStatus regRemStatus,
            Pageable pageable
    );
}
