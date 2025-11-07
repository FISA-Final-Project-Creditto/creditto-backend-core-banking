package org.creditto.core_banking.domain.regularremittance.repository;

import org.creditto.core_banking.domain.regularremittance.entity.MonthlyRegularRemittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonthlyRegularRemittanceRepository extends JpaRepository<MonthlyRegularRemittance, Long> {
    
    // 날짜에 맞는 월 정기 송금 조회
    List<MonthlyRegularRemittance> findByScheduledDate(Integer scheduledDate);
    
}
