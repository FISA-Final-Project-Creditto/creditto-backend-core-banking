package org.creditto.core_banking.domain.overseasremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 해외송금 내역 조회 관련 비즈니스 로직을 처리하는 서비스입니다.
 * 모든 조회 기능은 읽기 전용 트랜잭션(readOnly = true)으로 동작하여 성능을 최적화합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RemittanceQueryService {

    private final OverseasRemittanceRepository remittanceRepository;

    /**
     * 특정 고객의 모든 해외송금 내역을 조회합니다.
     *
     * @param clientId 조회할 고객의 ID
     * @return 고객의 송금 내역 DTO 리스트
     */
    public List<OverseasRemittanceResponseDto> getRemittanceList(String clientId) {
        return remittanceRepository.findByClientIdWithDetails(clientId)
                .stream()
                .map(OverseasRemittanceResponseDto::from)
                .toList();
    }
}
