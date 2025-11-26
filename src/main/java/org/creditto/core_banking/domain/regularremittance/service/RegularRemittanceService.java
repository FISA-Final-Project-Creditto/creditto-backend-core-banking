package org.creditto.core_banking.domain.regularremittance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.domain.recipient.dto.RecipientCreateDto;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.domain.recipient.service.RecipientFactory;
import org.creditto.core_banking.domain.regularremittance.dto.*;
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

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RegularRemittanceService {

    private final RegularRemittanceRepository regularRemittanceRepository;
    private final OverseasRemittanceRepository overseasRemittanceRepository;
    private final AccountRepository accountRepository;
    private final RecipientRepository recipientRepository;
    private final RecipientFactory recipientFactory;

    // Task 1: 사용자 정기송금 설정 내역 조회
    public List<RegularRemittanceResponseDto> getScheduledRemittancesByUserId(Long userId) {
        List<RegularRemittance> remittances = regularRemittanceRepository.findByAccountUserId(userId);
        return remittances.stream()
                .map(RegularRemittanceResponseDto::from)
                .collect(Collectors.toList());
    }

    // Task 2: 하나의 정기송금 설정에 대한 송금 기록 조회
    public List<RemittanceHistoryDto> getRegularRemittanceHistoryByRegRemId(Long userId, Long regRemId) {
        // 1. regRemId로 RegularRemittance 설정 조회
        RegularRemittance regularRemittance = regularRemittanceRepository.findById(regRemId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE));

        // 2. 보안 검증: 요청한 externalUserId와 정기송금 설정의 소유자(account.externalUserId)가 일치하는지 확인
        if (!regularRemittance.getAccount().getUserId().equals(userId)) {
            throw new CustomBaseException(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE); // 소유자가 다르면 "찾을 수 없음" 처리
        }

        // 3. regRemId에 해당하는 OverseasRemittance 기록을 RemittanceHistoryRes DTO로 변환
        return overseasRemittanceRepository.findByRecur_RegRemIdOrderByCreatedAtDesc(regRemId).stream()
                .map(overseas -> new RemittanceHistoryDto(
                        overseas.getSendAmount(),
                        overseas.getExchange().getExchangeRate(),
                        overseas.getCreatedAt().toLocalDate()
                ))
                .collect(Collectors.toList());
    }

    // Task 3: 단일 송금 내역 상세 조회
    public RemittanceDetailDto getRegularRemittanceDetail(Long userId, Long remittanceId) {
        // 1. remittanceId로 OverseasRemittance 엔티티 조회
        OverseasRemittance overseasRemittance = overseasRemittanceRepository.findById(remittanceId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ENTITY));

        // 2. 요청한 userId와 송금 기록의 소유자(clientId)가 일치하는지 확인
        if (!overseasRemittance.getUserId().equals(userId)) {
            throw new CustomBaseException(ErrorBaseCode.NOT_FOUND_ENTITY); // 소유자가 다르면 "찾을 수 없음" 처리
        }

        // 3. DTO로 변환하여 반환
        return RemittanceDetailDto.builder()
                .accountNo(overseasRemittance.getAccount().getAccountNo())
                .totalFee(overseasRemittance.getFeeRecord().getTotalFee())
                .sendAmount(overseasRemittance.getSendAmount())
                .recipientBankName(overseasRemittance.getRecipient().getBankName())
                .recipientAccountNo(overseasRemittance.getRecipient().getAccountNo())
                .remittanceStatus(overseasRemittance.getRemittanceStatus())
                .build();
    }

    // Task 4: 정기송금 신규 등록
    @Transactional
    public RegularRemittanceResponseDto createScheduledRemittance(Long userId, RegularRemittanceCreateDto dto) {
        Account account = accountRepository.findByAccountNo(dto.getAccountNo())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        if (!Objects.equals(account.getUserId(), userId)) {
            throw new CustomBaseException(ErrorBaseCode.FORBIDDEN);
        }

        RecipientCreateDto recipientCreateDto = new RecipientCreateDto(
                dto.getRecipientName(),
                dto.getRecipientAccountNo(),
                dto.getRecipientBankName(),
                dto.getRecipientBankCode(),
                dto.getRecipientPhoneCc(),
                dto.getRecipientPhoneNo(),
                dto.getRecipientCountry(),
                dto.getReceiveCurrency()
        );
        Recipient recipient = recipientFactory.findOrCreate(recipientCreateDto);

        RegularRemittance newRemittance;
        if ("MONTHLY".equalsIgnoreCase(dto.getRegRemType())) {
            newRemittance = MonthlyRegularRemittance.of(
                    account,
                    recipient,
                    dto.getSendCurrency(),
                    dto.getReceiveCurrency(),
                    dto.getSendAmount(),
                    dto.getScheduledDate()
            );
        } else if ("WEEKLY".equalsIgnoreCase(dto.getRegRemType())) {
            newRemittance = WeeklyRegularRemittance.of(
                    account,
                    recipient,
                    dto.getSendCurrency(),
                    dto.getReceiveCurrency(),
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
    public void updateScheduledRemittance(Long regRemId, Long userId, RegularRemittanceUpdateDto dto) {
        RegularRemittance remittance = regularRemittanceRepository.findById(regRemId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE));

        if (!Objects.equals(remittance.getAccount().getUserId(), userId)) {
            throw new CustomBaseException(ErrorBaseCode.FORBIDDEN);
        }

        // 계좌는 바뀐 계좌번호로 Account 변경
        Account account = accountRepository.findByAccountNo(dto.getAccountNo())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        // 수취인은 세부사항만 변경
        Recipient recipient = remittance.getRecipient();
        recipient.updateDetails(
                dto.getRecipientPhoneNo(),
                dto.getRecipientBankName(),
                dto.getRecipientBankCode(),
                dto.getAccountNo()
        );

        remittance.updateDetails(
                account,
                recipient,
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
    public void deleteScheduledRemittance(Long regRemId, Long userId) {
        RegularRemittance remittance = regularRemittanceRepository.findById(regRemId)
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE));

        if (!Objects.equals(remittance.getAccount().getUserId(), userId)) {
            throw new CustomBaseException(ErrorBaseCode.FORBIDDEN);
        }

        regularRemittanceRepository.delete(remittance);
    }


}

