package org.creditto.core_banking.domain.regularremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceCreateReqDto;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceResponseDto;
import org.creditto.core_banking.domain.regularremittance.dto.RegularRemittanceUpdateReqDto;
import org.creditto.core_banking.domain.regularremittance.dto.RemittanceHistoryResDto;
import org.creditto.core_banking.domain.regularremittance.entity.MonthlyRegularRemittance;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.creditto.core_banking.domain.regularremittance.entity.ScheduledDay;
import org.creditto.core_banking.domain.regularremittance.entity.WeeklyRegularRemittance;
import org.creditto.core_banking.domain.regularremittance.repository.RegularRemittanceRepository;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegularRemittanceService {

    private final RegularRemittanceRepository regularRemittanceRepository;
    private final OverseasRemittanceRepository overseasRemittanceRepository;
    private final AccountRepository accountRepository;
    private final RecipientRepository recipientRepository;

    // 1. 사용자 정기송금 설정 내역 조회
    public List<RegularRemittanceResponseDto> getScheduledRemittancesByUserId(String userId) {
        List<RegularRemittance> remittances = regularRemittanceRepository.findByAccountExternalUserId(userId);
        return remittances.stream()
                .map(RegularRemittanceResponseDto::from)
                .collect(Collectors.toList());
    }

    // Task 2: 하나의 정기송금 설정에 대한 송금 기록 조회
    public List<OverseasRemittanceResponseDto> getRemittanceRecordsByRecurId(Long recurId) {
        List<OverseasRemittance> records = overseasRemittanceRepository.findAllByRecur_RegRemIdOrderByCreatedAtDesc(recurId);
        return records.stream()
                .map(OverseasRemittanceResponseDto::from)
                .collect(Collectors.toList());
    }

    // Task 2: 하나의 정기송금 설정에 대한 송금 기록 조회
    public List<RemittanceHistoryResDto> getRegularRemittanceHistoryByRegRemId(String userId, Long regRemId) {
        // 1. regRemId로 RegularRemittance 설정 조회
        RegularRemittance regularRemittance = regularRemittanceRepository.findById(regRemId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE));

        // 2. 보안 검증: 요청한 externalUserId와 정기송금 설정의 소유자(account.externalUserId)가 일치하는지 확인
        if (!regularRemittance.getAccount().getUserId().equals(userId)) {
            throw new CustomBaseException(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE); // 소유자가 다르면 "찾을 수 없음" 처리
        }

        // 3. 정기송금 타입 및 날짜 정보 추출
        String regremType = null;
        Integer scheduledDate = null;
        ScheduledDay scheduledDay = null;

        if (regularRemittance instanceof MonthlyRegularRemittance monthly) {
            regremType = "MONTHLY";
            scheduledDate = monthly.getScheduledDate();
        } else if (regularRemittance instanceof WeeklyRegularRemittance weekly) {
            regremType = "WEEKLY";
            scheduledDay = weekly.getScheduledDay();
        }

        // 4. regRemId에 해당하는 OverseasRemittance 기록을 RemittanceHistoryRes DTO로 변환
        String finalRegremType = regremType;
        Integer finalScheduledDate = scheduledDate;
        ScheduledDay finalScheduledDay = scheduledDay;
        return overseasRemittanceRepository.findByRecur_RegRemIdOrderByCreatedAtDesc(regRemId).stream()
                .map(overseas -> new RemittanceHistoryResDto(
                        overseas.getRemittanceId(),
                        regRemId,
                        overseas.getRecipient().getName(),
                        overseas.getRecipient().getBankName(),
                        overseas.getSendCurrency(),
                        overseas.getSendAmount(),
                        overseas.getReceiveCurrency(),
                        overseas.getReceiveAmount(),
                        overseas.getRemittanceStatus(),
                        finalRegremType,
                        finalScheduledDate,
                        finalScheduledDay,
                        overseas.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // Task 3: 단일 송금 내역 상세 조회
    public OverseasRemittanceResponseDto getRegularRemittanceDetail(String userId, Long remittanceId) {
        // 1. remittanceId로 OverseasRemittance 엔티티 조회
        OverseasRemittance overseasRemittance = overseasRemittanceRepository.findById(remittanceId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ENTITY));

        // 2. 요청한 userId와 송금 기록의 소유자(clientId)가 일치하는지 확인
        if (!overseasRemittance.getUserId().equals(userId)) {
            throw new CustomBaseException(ErrorBaseCode.NOT_FOUND_ENTITY); // 소유자가 다르면 "찾을 수 없음" 처리
        }

        // 3. DTO로 변환하여 반환
        return OverseasRemittanceResponseDto.from(overseasRemittance);
    }

    // 정기 해외 송금 내역 신규 등록
    @Transactional
    public RegularRemittanceResponseDto createScheduledRemittance(String userId, RegularRemittanceCreateReqDto dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        if (!Objects.equals(account.getUserId(), userId)) {
            throw new CustomBaseException(ErrorBaseCode.FORBIDDEN);
        }

        Recipient recipient = recipientRepository.findById(dto.getRecipientId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_RECIPIENT));

        RegularRemittance newRemittance;
        if ("MONTHLY".equalsIgnoreCase(dto.getRegRemType())) {
            newRemittance = MonthlyRegularRemittance.of(
                    account,
                    recipient,
                    dto.getSendCurrency(),
                    dto.getReceivedCurrency(),
                    dto.getSendAmount(),
                    dto.getScheduledDate()
            );
        } else if ("WEEKLY".equalsIgnoreCase(dto.getRegRemType())) {
            newRemittance = WeeklyRegularRemittance.of(
                    account,
                    recipient,
                    dto.getSendCurrency(),
                    dto.getReceivedCurrency(),
                    dto.getSendAmount(),
                    dto.getScheduledDay()
            );
        } else {
            throw new CustomBaseException(ErrorBaseCode.BAD_REQUEST);
        }

        RegularRemittance savedRemittance = regularRemittanceRepository.save(newRemittance);
        return RegularRemittanceResponseDto.from(savedRemittance);
    }

    // 정기 해외 송금 설정 수정
    @Transactional
    public void updateScheduledRemittance(Long recurId, String userId, RegularRemittanceUpdateReqDto dto) {
        RegularRemittance remittance = regularRemittanceRepository.findById(recurId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE));

        if (!Objects.equals(remittance.getAccount().getUserId(), userId)) {
            throw new CustomBaseException(ErrorBaseCode.FORBIDDEN);
        }

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));
        Recipient recipient = recipientRepository.findById(dto.getRecipientId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_RECIPIENT));

////        remittance.updateDetails(
//                account,
//                recipient,
//                dto.getSendCurrency(),
//                dto.getReceivedCurrency(),
//                dto.getSendAmount(),
//                dto.getRegRemStatus()
//        );

        if (remittance instanceof MonthlyRegularRemittance monthly) {
            monthly.updateSchedule(dto.getScheduledDate());
        } else if (remittance instanceof WeeklyRegularRemittance weekly) {
            weekly.updateSchedule(dto.getScheduledDay());
        }
    }

    // 정기 해외 송금 설정 삭제
    @Transactional
    public void deleteScheduledRemittance(Long recurId, String userId) {
        RegularRemittance remittance = regularRemittanceRepository.findById(recurId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE));

        if (!Objects.equals(remittance.getAccount().getUserId(), userId)) {
            throw new CustomBaseException(ErrorBaseCode.FORBIDDEN);
        }

        regularRemittanceRepository.delete(remittance);
    }


}

