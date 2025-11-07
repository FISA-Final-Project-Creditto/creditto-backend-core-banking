package org.creditto.core_banking.domain.regularremittance.repository;

import org.creditto.core_banking.domain.regularremittance.entity.ScheduledDay;
import org.creditto.core_banking.domain.regularremittance.entity.WeeklyRegularRemittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeeklyRegularRemittanceRepository extends JpaRepository<WeeklyRegularRemittance, Long> {
    
    // 요일에 맞는 주 정기 송금 조회
    List<WeeklyRegularRemittance> findByScheduledDay(ScheduledDay scheduledDay);
    
}
