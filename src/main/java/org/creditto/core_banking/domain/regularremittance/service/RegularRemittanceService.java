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
import org.creditto.core_banking.domain.regularremittance.entity.MonthlyRegularRemittance;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.creditto.core_banking.domain.regularremittance.entity.WeeklyRegularRemittance;
import org.creditto.core_banking.domain.regularremittance.repository.RegularRemittanceRepository;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 등록된 정기 해외 송금 설정 조회
    public List<RegularRemittanceResponseDto> getScheduledRemittancesByUserId(String userId) {
        List<RegularRemittance> remittances = regularRemittanceRepository.findAllByAccount_ExternalUserId(userId);
        return remittances.stream()
                .map(RegularRemittanceResponseDto::from)
                .collect(Collectors.toList());
    }

    // 한 건의 정기 해외 송금 기록 상세 조회
    public List<OverseasRemittanceResponseDto> getRemittanceRecordsByRecurId(Long recurId) {
        List<OverseasRemittance> records = overseasRemittanceRepository.findAllByRecur_RegRemIdOrderByCreatedAtDesc(recurId);
        return records.stream()
                .map(OverseasRemittanceResponseDto::from)
                .collect(Collectors.toList());
    }

    // 정기 해외 송금 내역 신규 등록
    @Transactional
    public RegularRemittanceResponseDto createScheduledRemittance(String userId, RegularRemittanceCreateReqDto dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        if (!Objects.equals(account.getExternalUserId(), userId)) {
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

        if (!Objects.equals(remittance.getAccount().getExternalUserId(), userId)) {
            throw new CustomBaseException(ErrorBaseCode.FORBIDDEN);
        }

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));
        Recipient recipient = recipientRepository.findById(dto.getRecipientId())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_RECIPIENT));

        remittance.updateDetails(
                account,
                recipient,
                dto.getSendCurrency(),
                dto.getReceivedCurrency(),
                dto.getSendAmount(),
                dto.getRegRemStatus()
        );

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

        if (!Objects.equals(remittance.getAccount().getExternalUserId(), userId)) {
            throw new CustomBaseException(ErrorBaseCode.FORBIDDEN);
        }

        regularRemittanceRepository.delete(remittance);
    }


}

