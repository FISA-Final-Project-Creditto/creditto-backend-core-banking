package org.creditto.core_banking.domain.overseasremittance.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.account.service.AccountService;
import org.creditto.core_banking.domain.overseasremittance.dto.ExecuteRemittanceCommand;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceRequestDto;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.domain.recipient.dto.RecipientCreateDto;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.service.RecipientFactory;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 해외송금 유스케이스를 조정하는 Application Service 입니다.
 * 외부(Controller)의 요청을 받아 도메인 객체(Command)를 생성하고,
 * 도메인 로직 실행을 다른 도메인 서비스({@link RemittanceProcessorService})에 위임하는 오케스트레이션 역할을 수행합니다.
 */
@Service
@RequiredArgsConstructor
public class OneTimeRemittanceService {

    private final RemittanceProcessorService remittanceProcessorService;
    private final AccountRepository accountRepository;
    private final RecipientFactory recipientFactory;
    private final OverseasRemittanceRepository overseasRemittanceRepository;
    private final AccountService accountService;

    /**
     * 클라이언트의 해외송금 요청을 받아 전체 송금 프로세스를 조정합니다.
     *
     * @param request 클라이언트로부터 받은 해외송금 요청 데이터
     * @return 송금 처리 결과
     */
    @Transactional
    public OverseasRemittanceResponseDto processRemittance(Long userId, OverseasRemittanceRequestDto request) {
        // 출금 계좌 조회 및 ID 확보
        Account account = accountRepository.findByAccountNo(request.getAccountNo())
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.NOT_FOUND_ACCOUNT));

        // 비밀번호 검증
        accountService.verifyPassword(account.getId(), request.getPassword());

        // RecipientFactory를 통해 수취인 조회 또는 생성
        RecipientCreateDto recipientCreateDto = request.getRecipientInfo().toRecipientCreateDto();
        Recipient recipient = recipientFactory.findOrCreate(recipientCreateDto);


        // ExecuteRemittanceCommand 생성
        ExecuteRemittanceCommand command = ExecuteRemittanceCommand.of(
                userId,
                recipient.getRecipientId(),
                account.getId(),
                request.getRecurId(),
                request.getSendCurrency(),
                recipient.getCurrencyCode(),
                request.getTargetAmount(),
                request.getStartDate()
        );

        // Command 실행 위임: 생성된 Command를 통해 실제 송금 로직 실행
        return remittanceProcessorService.execute(command);
    }

    public List<OverseasRemittanceResponseDto> getOneTimeRemittanceList(Long userId) {
        return overseasRemittanceRepository.findByUserIdAndRecurIsNull(userId)
                .stream()
                .map(OverseasRemittanceResponseDto::from)
                .toList();
    }
}
