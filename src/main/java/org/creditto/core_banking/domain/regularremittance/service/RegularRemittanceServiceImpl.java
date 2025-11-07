package org.creditto.core_banking.domain.regularremittance.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
//import org.creditto.core_banking.domain.overseasremittance.service.OverseasRemittanceService;
//import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceDto;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceRequestDto;
import org.creditto.core_banking.domain.regularremittance.entity.MonthlyRegularRemittance;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.creditto.core_banking.domain.regularremittance.entity.WeeklyRegularRemittance;
import org.creditto.core_banking.domain.regularremittance.repository.MonthlyRegularRemittanceRepository;
import org.creditto.core_banking.domain.regularremittance.repository.RegularRemittanceRepository;
import org.creditto.core_banking.domain.regularremittance.repository.WeeklyRegularRemittanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegularRemittanceServiceImpl implements RegularRemittanceService {

    private final RegularRemittanceRepository regularRemittanceRepository;
    private final MonthlyRegularRemittanceRepository monthlyRegularRemittanceRepository;
    private final WeeklyRegularRemittanceRepository weeklyRegularRemittanceRepository;
    private final AccountRepository accountRepository;
    private final RecipientRepository recipientRepository;

//    private final OverseasRemittanceService overseasRemittanceService; // 채연이 코드랑 합칠 때 주석 제거

    // 해외 정기 송금 내용 등록, regular_remittance 테이블에 저장
    @Override
    @Transactional
    public void createRegularRemittance(RegularRemittanceRequestDto dto) {
        Account account = accountRepository.findById(dto.getAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        Recipient recipient = recipientRepository.findById(dto.getRecipientId())
            .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        RegularRemittance regularRemittance;
        if ("MONTHLY".equalsIgnoreCase(dto.getRegremType())) {
            regularRemittance = MonthlyRegularRemittance.of(
                account,
                recipient,
                dto.getSendCurrency(),
                dto.getReceivedCurrency(),
                dto.getSendAmount(),
                dto.getScheduledDate()
            );
        } else if ("WEEKLY".equalsIgnoreCase(dto.getRegremType())) {
            regularRemittance = WeeklyRegularRemittance.of(
                account,
                recipient,
                dto.getSendCurrency(),
                dto.getReceivedCurrency(),
                dto.getSendAmount(),
                dto.getScheduledDay()
            );
        } else {
            throw new IllegalArgumentException("Invalid regremType");
        }

        regularRemittanceRepository.save(regularRemittance);
    }

    // 정기 송금을 처리할 메서드
    @Override
    @Transactional
    public void processDueRemittances() {
        LocalDate today = LocalDate.now();

        // 월간 정기송금 처리
        List<MonthlyRegularRemittance> monthlyDue = monthlyRegularRemittanceRepository.findByScheduledDate(today.getDayOfMonth());
//        monthlyDue.forEach(overseasRemittanceService::processRemittance);

        // 주간 정기송금 처리
        // Java의 DayOfWeek와 DB에 저장된 ScheduledDay Enum의 이름이 일치해야 함
//        List<WeeklyRegularRemittance> weeklyDue = weeklyRegularRemittanceRepository.findByScheduledDay(today.getDayOfWeek());
//        weeklyDue.forEach(overseasRemittanceService::processRemittance);
    }

    // overseas쪽에서 해외 정기 송금 리스트를 받아 그대로 반환
//    @Override
//    @Transactional(readOnly = true) // 데이터 조회만
//    public List<OverseasRemittanceDto> getRemittanceList(Long regRemId) {
//        // regRemId 없으면 예외처리
//        if (!regularRemittanceRepository.existsById(regRemId)) {
//            throw new EntityNotFoundException("해당 ID의 정기 송금 설정을 찾을 수 없습니다" + regRemId);
//        }
//
//        // 받은 결과 그대로 return
//        return overseasRemittanceService.get~(regRemId);
//    }

}