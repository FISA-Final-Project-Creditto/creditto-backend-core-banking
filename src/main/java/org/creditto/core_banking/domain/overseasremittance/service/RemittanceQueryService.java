package org.creditto.core_banking.domain.overseasremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
//해외송금 내역 조회 서비스
public class RemittanceQueryService {

    private final OverseasRemittanceRepository remittanceRepository;

    public List<OverseasRemittanceResponseDto> getRemittanceList(String clientId) {
        return remittanceRepository.findByClientIdWithDetails(clientId)
                .stream()
                .map(OverseasRemittanceResponseDto::from)
                .toList();
    }
}
