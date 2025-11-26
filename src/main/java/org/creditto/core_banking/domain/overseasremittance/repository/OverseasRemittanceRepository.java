package org.creditto.core_banking.domain.overseasremittance.repository;

import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * {@link OverseasRemittance} 엔티티에 대한 데이터베이스 연산을 처리하는 리포지토리입니다.
 */
@Repository
public interface OverseasRemittanceRepository extends JpaRepository<OverseasRemittance, Long> {

    /**
     * 특정 고객(user)의 모든 송금 내역을 연관된 엔티티(수취인, 계좌, 환전, 수수료, 정기송금)와 함께 조회합니다.
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

    /**
     * 특정 정기송금의 특정 송금 내역을 조회합니다.
     *
     * @param regRemId 조회할 정기송금의 ID
     * @param remittanceId 조회할 송금 내역의 ID
     * @return 조회된 송금 내역
     */
    @Query("SELECT r FROM OverseasRemittance r " +
            "JOIN FETCH r.recipient " +
            "JOIN FETCH r.account " +
            "JOIN FETCH r.exchange " +
            "JOIN FETCH r.feeRecord " +
            "WHERE r.id = :remittanceId AND r.recur.regRemId = :regRemId")
    Optional<OverseasRemittance> findByIdAndRecur_RegRemId(
            @Param("remittanceId") Long remittanceId,
            @Param("regRemId") Long regRemId
    );

    /**
     * 특정 정기송금(Regular Remittance)에 해당하는 모든 송금 내역을 최신순으로 조회합니다.
     * Fetch Join을 사용하여 N+1 쿼리 문제를 방지합니다.
     *
     * @param regRemId 조회할 정기송금의 ID
     * @return 조회된 송금 내역 리스트 ({@link OverseasRemittance})
     */
    @Query("SELECT r FROM OverseasRemittance r " +
            "JOIN FETCH r.recipient " +
            "JOIN FETCH r.account " +
            "JOIN FETCH r.exchange " +
            "JOIN FETCH r.feeRecord " +
            "WHERE r.recur.regRemId = :regRemId " +
            "ORDER BY r.createdAt DESC")
    List<OverseasRemittance> findByRecur_RegRemIdOrderByCreatedAtDesc(@Param("regRemId") Long regRemId);

    /**
     * 특정 고객(Client)의 모든 일회성 송금 내역을 연관된 엔티티(수취인, 계좌, 환전, 수수료)와 함께 조회합니다.
     * Fetch Join을 사용하여 N+1 쿼리 문제를 방지합니다.
     *
     * @param userId 조회할 고객의 ID
     * @return 조회된 일회성 송금 내역 리스트 ({@link OverseasRemittance})
     */
    @Query("SELECT r FROM OverseasRemittance r " +
            "JOIN FETCH r.recipient " +
            "JOIN FETCH r.account " +
            "JOIN FETCH r.exchange " +
            "JOIN FETCH r.feeRecord " +
            "WHERE r.userId = :userId AND r.recur IS NULL")
    List<OverseasRemittance> findByUserIdAndRecurIsNull(@Param("userId") Long userId);

}
