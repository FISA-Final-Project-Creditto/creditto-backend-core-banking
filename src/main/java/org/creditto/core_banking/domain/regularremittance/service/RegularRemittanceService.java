package org.creditto.core_banking.domain.regularremittance.service;

import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceRequestDto;
//import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceDto;

import java.util.List;

public interface RegularRemittanceService {

    void createRegularRemittance(RegularRemittanceRequestDto requestDto);

    void processDueRemittances();

    // 해외 정기 송금 내역 리스트 가져오기
//    List<OverseasRemittanceDto> getRemittanceList(Long regRemId);
}