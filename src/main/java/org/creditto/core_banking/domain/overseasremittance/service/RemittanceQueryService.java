package org.creditto.core_banking.domain.overseasremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.CreditAnalysisRes;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 해외송금 내역 조회 관련 비즈니스 로직을 처리하는 서비스입니다.
 * 모든 조회 기능은 읽기 전용 트랜잭션(readOnly = true)으로 동작하여 성능을 최적화합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RemittanceQueryService {

    private final OverseasRemittanceRepository remittanceRepository;

    /**
     * 특정 고객의 모든 해외송금 내역을 조회합니다.
     *
     * @param userId 조회할 고객의 ID
     * @return 고객의 송금 내역 DTO 리스트
     */
    public List<OverseasRemittanceResponseDto> getRemittanceList(Long userId) {
        return remittanceRepository.findByUserIdWithDetails(userId)
                .stream()
                .map(OverseasRemittanceResponseDto::from)
                .toList();
    }

    /**
     * 특정 고객의 해외송금 내역 일부를 신용도 분석용 DTO로 조회합니다.
     *
     * @param userId 조회할 송금의 ID
     * @return 신용도 분석용 데이터 DTO
     * @throws CustomBaseException 송금 내역을 찾을 수 없는 경우
     */
    public List<CreditAnalysisRes> getCreditAnalysisData(Long userId) {
        return remittanceRepository.findByUserId(userId)
                .stream()
                .map(CreditAnalysisRes::from)
                .toList();
    }
}
