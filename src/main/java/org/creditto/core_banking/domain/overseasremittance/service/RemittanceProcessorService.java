package org.creditto.core_banking.domain.overseasremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
import org.creditto.core_banking.domain.exchange.entity.Exchange;
import org.creditto.core_banking.domain.exchange.repository.ExchangeRepository;
import org.creditto.core_banking.domain.exchange.service.ExchangeService;
import org.creditto.core_banking.domain.overseasremittance.dto.ExecuteRemittanceCommand;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.domain.regularremittance.entity.RegularRemittance;
import org.creditto.core_banking.domain.regularremittance.repository.RegularRemittanceRepository;
import org.creditto.core_banking.domain.remittancefee.dto.RemittanceFeeReq;
import org.creditto.core_banking.domain.remittancefee.entity.FeeRecord;
import org.creditto.core_banking.domain.remittancefee.service.RemittanceFeeService;
import org.creditto.core_banking.domain.transaction.entity.TxnResult;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.service.TransactionService;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * 해외송금의 핵심 비즈니스 로직을 실제로 실행하는 Domain Service 입니다.
 * 이 서비스는 외부와 격리되어, 오직 내부 Command 객체({@link ExecuteRemittanceCommand})만을 받아
 * 송금 실행에 필요한 모든 도메인 규칙(엔티티 조회, 잔액 확인, 출금, 거래 내역 생성 등)을 트랜잭션 내에서 수행합니다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class RemittanceProcessorService {

    private final OverseasRemittanceRepository remittanceRepository;
    private final AccountRepository accountRepository;
    private final RecipientRepository recipientRepository;
    private final RegularRemittanceRepository regularRemittanceRepository;
    private final ExchangeRepository exchangeRepository;
    private final ExchangeService exchangeService;
    private final TransactionService transactionService;
    private final RemittanceFeeService remittanceFeeService;

    /**
     * 전달된 Command를 기반으로 해외송금의 모든 단계를 실행합니다.
     * 1. Command에 포함된 ID를 사용하여 관련 엔티티(계좌, 수취인 등)를 조회합니다.
     * 2. ExchangeService를 통해 환전 처리 및 결과(DTO)를 수신합니다.
     * 3. RemittanceFeeService를 통해 수수료를 계산하고, 계좌 잔액을 확인합니다.
     * 4. 해외송금(OverseasRemittance) 엔티티를 생성하고 저장합니다.
     * 5. 실제 계좌에서 수수료 및 송금액을 출금하고, 각 출금에 대한 거래(Transaction) 내역을 생성합니다.
     *
     * @param command 송금 실행에 필요한 모든 데이터가 포함된 Command 객체
     * @return 송금 처리 결과를 담은 응답 DTO
     * @throws CustomBaseException 잔액이 부족하거나 지원하지 않는 통화인 경우 발생
     * @throws IllegalArgumentException 관련 엔티티를 찾을 수 없는 경우 발생
     */
    public OverseasRemittanceResponseDto execute(ExecuteRemittanceCommand command) {

        // 관련 엔티티 조회
        Account account = accountRepository.findById(command.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));

        Recipient recipient = recipientRepository.findById(command.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException("수취인을 찾을 수 없습니다."));

        // 정기 송금 정보 조회 (regRemId가 있을 경우)
        RegularRemittance regularRemittance = Optional.ofNullable(command.getRegRemId())
                .map(id -> regularRemittanceRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("정기송금 정보를 찾을 수 없습니다.")))
                .orElse(null);

        // 1. ExchangeService를 통해 환전 처리 및 결과(DTO) 수신
        ExchangeReq exchangeReq = new ExchangeReq(command.getSendCurrency(), command.getReceiveCurrency(), command.getSendAmount());
        ExchangeRes exchangeRes = exchangeService.exchange(exchangeReq);

        // 2. 수수료 계산을 위해 RemittanceFeeService 호출
        RemittanceFeeReq feeReq = new RemittanceFeeReq(
            exchangeRes.exchangeRate(), // command.getExchangeRate() 대신 exchangeRes에서 가져옴
            command.getSendAmount(),
            command.getReceiveCurrency(),
            exchangeRes.fromAmountInUSD() // 환전 결과에서 USD 환율 가져오기
        );

        FeeRecord feeRecord = remittanceFeeService.calculateAndSaveFee(feeReq);
        BigDecimal totalFee = feeRecord.getTotalFee();

        // 금액 및 수수료 계산
        BigDecimal sendAmount = command.getSendAmount();
        BigDecimal totalDeduction = sendAmount.add(totalFee);

        // 잔액 확인
        if (account.getBalance().compareTo(totalDeduction) < 0) {
            // 실패 트랜잭션 기록
            transactionService.saveTransaction(account, sendAmount, TxnType.WITHDRAWAL, null, TxnResult.FAILURE);
            throw new CustomBaseException(ErrorBaseCode.INSUFFICIENT_FUNDS);
        }

        // 3. DTO에 담겨올 ID로 Exchange 엔티티 다시 조회
         Long exchangeId = exchangeRes.exchangeId();
         Exchange savedExchange = exchangeRepository.findById(exchangeId)
                 .orElseThrow(() -> new IllegalArgumentException("환전 내역을 찾을 수 없습니다."));

        // 4. 최종 수취 금액은 exchangeRes에서 가져옴
        BigDecimal receiveAmount = exchangeRes.exchangeAmount();

        // 5. 송금 이력 생성
        OverseasRemittance overseasRemittance = OverseasRemittance.of(
                recipient,
                account,
                regularRemittance,
                savedExchange,
                feeRecord,
                command.getClientId(),
                command.getSendCurrency(),
                command.getReceiveCurrency(), // receiveCurrency
                sendAmount,
                receiveAmount,
                command.getStartDate()
        );
        remittanceRepository.save(overseasRemittance);

        // 수수료 차감 및 거래 내역 생성
        if (totalFee.compareTo(BigDecimal.ZERO) > 0) {
            account.withdraw(totalFee);
            transactionService.saveTransaction(account, totalFee, TxnType.FEE, overseasRemittance.getRemittanceId(), TxnResult.SUCCESS);
        }

        // 송금액 차감 및 거래 내역 생성
        account.withdraw(sendAmount);
        transactionService.saveTransaction(account, sendAmount, TxnType.WITHDRAWAL, overseasRemittance.getRemittanceId(), TxnResult.SUCCESS);

        accountRepository.save(account);

        return OverseasRemittanceResponseDto.from(overseasRemittance);
    }
}