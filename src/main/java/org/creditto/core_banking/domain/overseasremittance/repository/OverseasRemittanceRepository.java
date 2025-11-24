package org.creditto.core_banking.domain.overseasremittance.repository;

import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * {@link OverseasRemittance} 엔티티에 대한 데이터베이스 연산을 처리하는 리포지토리입니다.
 */
@Repository
public interface OverseasRemittanceRepository extends JpaRepository<OverseasRemittance, Long> {

    /**
     * 특정 고객(Client)의 모든 송금 내역을 연관된 엔티티(수취인, 계좌, 환전, 수수료, 정기송금)와 함께 조회합니다.
     * Fetch Join을 사용하여 N+1 쿼리 문제를 방지합니다.
     *
     * @param userId 조회할 고객의 ID
     * @return 조회된 송금 내역 리스트 ({@link OverseasRemittance})
     */
    @Query("SELECT r FROM OverseasRemittance r " +
            "JOIN FETCH r.recipient " +
            "JOIN FETCH r.account " +
            "JOIN FETCH r.exchange " +
            "JOIN FETCH r.feeRecord " +
            "LEFT JOIN FETCH r.recur " +
            "WHERE r.userId = :userId")
    List<OverseasRemittance> findByUserIdWithDetails(@Param("userId") Long userId);
}
