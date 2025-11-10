package org.creditto.core_banking.domain.overseasremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceRequestDto;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.creditto.core_banking.domain.regularremittance.repository.RegularRemittanceRepository;
import org.creditto.core_banking.domain.remittancefee.entity.RemittanceFee;
import org.creditto.core_banking.domain.remittancefee.repository.RemittanceFeeRepository;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class OverseasRemittanceService {

    private final OverseasRemittanceRepository overseasRemittanceRepository;
    private final AccountRepository accountRepository;
    private final RemittanceFeeRepository remittanceFeeRepository;
    private final RecipientRepository recipientRepository;
    private final TransactionRepository transactionRepository;
    private final RegularRemittanceRepository regularRemittanceRepository;

    public List<OverseasRemittanceResponseDto> getRemittanceList(String clientId) {
        return overseasRemittanceRepository.findByClientIdWithDetails(clientId)
                .stream()
                .map(OverseasRemittanceResponseDto::from)
                .toList();
    }

    public OverseasRemittanceResponseDto processRemittance(OverseasRemittanceRequestDto request) {

        // 관련 엔티티 조회
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage()));

        Recipient recipient = recipientRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage()));

        RemittanceFee fee = remittanceFeeRepository.findById(request.getFeeId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage()));

        // 정기 송금 정보 조회 (regRemId가 있을 경우)
        RegularRemittance regularRemittance = null;
        if (request.getRegRemId() != null) {
            regularRemittance = regularRemittanceRepository.findById(request.getRegRemId())
                    .orElseThrow(() -> new IllegalArgumentException(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage()));
        }

        // 금액 및 수수료 계산
        // 수수료 논의가 안되어서 변동 수수료를 고정값으로 처리
        BigDecimal sendAmount = request.getSendAmount();
        BigDecimal totalFee = fee.getBaseFee().add(fee.getVariableFee());
        BigDecimal totalDeduction = sendAmount.add(totalFee);

        // 잔액 확인
        if (account.getBalance().compareTo(totalDeduction) < 0) {
            // TODO: InsufficientBalanceException으로 변경 및 GlobalExceptionHandler에서 처리
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        // 송금 이력 생성 (영속성 컨텍스트에 저장, DB 반영은 트랜잭션 커밋 시)
        OverseasRemittance overseasRemittance = OverseasRemittance.of(
                recipient,
                account,
                request.getClientId(),
                fee,
                regularRemittance, // 정기 송금 정보 전달
                request.getExchangeRate(),
                sendAmount,
                null // receivedAmount는 수취인이 입금받았을 때 기록되므로, 송금 시점에는 null로 설정
        );
        overseasRemittanceRepository.save(overseasRemittance);

        // 수수료 차감 및 거래 내역 생성
        account.deductBalance(totalFee);
        LocalDateTime now = LocalDateTime.now();
        Transaction feeTransaction = Transaction.of(
                account,
                totalFee,
                TxnType.FEE,
                account.getBalance(), // 수수료 차감 후 잔액
                now
        );
        transactionRepository.save(feeTransaction);

        // 송금액 차감 및 거래 내역 생성
        account.deductBalance(sendAmount);
        Transaction withdrawalTransaction = Transaction.of(
                account,
                sendAmount,
                TxnType.WITHDRAWAL,
                account.getBalance(), // 송금액 차감 후 최종 잔액
                now
        );
        transactionRepository.save(withdrawalTransaction);

        // account 엔티티의 변경 : @Transactional -> 트랜잭션 커밋 시점에 DB에 반영
        // 명시적 저장
        accountRepository.save(account);

        return OverseasRemittanceResponseDto.from(overseasRemittance);
    }
}

