package org.creditto.core_banking.domain.overseasremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.overseasremittance.dto.ExecuteRemittanceCommand;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.domain.regularremittance.repository.RegularRemittanceRepository;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.domain.remittancefee.entity.RemittanceFee;
import org.creditto.core_banking.domain.remittancefee.repository.RemittanceFeeRepository;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
// 해외송금 공통 로직
// TODO: 송금의 모든 과정을 처리하여 너무 많은 책임을 지고 있음 -> 책임 나누기
public class RemittanceProcessorService {

    private final OverseasRemittanceRepository remittanceRepository;
    private final AccountRepository accountRepository;
    private final RemittanceFeeRepository remittanceFeeRepository;
    private final RecipientRepository recipientRepository;
    private final TransactionRepository transactionRepository;
    private final RegularRemittanceRepository regularRemittanceRepository;

    // command에서 필요한 데이터를 꺼내서 로직을 수행
    public OverseasRemittanceResponseDto execute(ExecuteRemittanceCommand command) {

        // 관련 엔티티 조회
        Account account = accountRepository.findById(command.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        Recipient recipient = recipientRepository.findById(command.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("수취인을 찾을 수 없습니다."));

        RemittanceFee fee = remittanceFeeRepository.findById(command.getFeeId())
                .orElseThrow(() -> new IllegalArgumentException("수수료 정보를 찾을 수 없습니다."));

        // 정기 송금 정보 조회 (regRemId가 있을 경우)
        RegularRemittance regularRemittance = Optional.ofNullable(command.getRegRemId())
                .map(id -> regularRemittanceRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("정기송금 정보를 찾을 수 없습니다.")))
                .orElse(null);

        // 금액 및 수수료 계산
        BigDecimal sendAmount = command.getSendAmount();
        BigDecimal totalFee = fee.getBaseFee().add(fee.getVariableFee());
        BigDecimal totalDeduction = sendAmount.add(totalFee);

        // 잔액 확인
        if (account.getBalance().compareTo(totalDeduction) < 0) {
            // TODO: InsufficientBalanceException으로 변경 및 GlobalExceptionHandler에서 처리
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }
        // 송금 이력 생성
        OverseasRemittance overseasRemittance = OverseasRemittance.of(
                recipient,
                account,
                command.getClientId(),
                fee,
                regularRemittance,
                command.getExchangeRate(),
                sendAmount,
                null
        );
        remittanceRepository.save(overseasRemittance);

        // 수수료 차감 및 거래 내역 생성
        account.withdraw(totalFee);
        Transaction feeTransaction = Transaction.of(
                account,
                totalFee,
                TxnType.FEE,
                account.getBalance(),
                LocalDateTime.now()
        );
        transactionRepository.save(feeTransaction);

        // 송금액 차감 및 거래 내역 생성
        account.withdraw(sendAmount);
        Transaction withdrawalTransaction = Transaction.of(
                account,
                sendAmount,
                TxnType.WITHDRAWAL,
                account.getBalance(),
                LocalDateTime.now()
        );
        transactionRepository.save(withdrawalTransaction);

        accountRepository.save(account);

        return OverseasRemittanceResponseDto.from(overseasRemittance);
    }
}