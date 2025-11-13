package org.creditto.core_banking.domain.overseasremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.overseasremittance.dto.ExecuteRemittanceCommand;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceRequestDto;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
//일회성 해외송금
public class OneTimeRemittanceService {

    private final RemittanceProcessorService remittanceProcessorService;

    public OverseasRemittanceResponseDto processRemittance(OverseasRemittanceRequestDto request) {
        // 요청 DTO를 실행 Command로 변환
        ExecuteRemittanceCommand command = ExecuteRemittanceCommand.from(request);

        // 공통 프로세서를 통해 실행
        return remittanceProcessorService.execute(command);
    }
}

