package org.creditto.core_banking.domain.overseasremittance;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.overseasremittance.dto.ExecuteRemittanceCommand;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceRequestDto;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.entity.RemittanceStatus;
import org.creditto.core_banking.domain.overseasremittance.service.OneTimeRemittanceService;
import org.creditto.core_banking.domain.overseasremittance.service.RemittanceProcessorService;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.remittancefee.entity.RemittanceFee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.creditto.core_banking.domain.account.entity.AccountState.ACTIVE;
import static org.creditto.core_banking.domain.account.entity.AccountType.DEPOSIT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OneTimeRemittanceServiceTest {

    @Mock
    private RemittanceProcessorService remittanceProcessorService; // OneTimeRemittanceService의 핵심 의존성

    @InjectMocks
    private OneTimeRemittanceService oneTimeRemittanceService;

    // 공통 목 객체 (실제 객체는 아니지만 테스트에 필요한 데이터 제공)
    private String clientId;
    private Account mockAccount;
    private Recipient mockRecipient;
    private RemittanceFee mockFee;
    private OverseasRemittanceRequestDto baseRequest;

    @BeforeEach
    void setUp() {
        clientId = "testClient";
        mockAccount = Account.of("1002-123-456789", "예금계좌", BigDecimal.valueOf(600_000), DEPOSIT , ACTIVE, clientId);
        mockRecipient = Recipient.of("John Doe", "310-555-1234", "+1", "Test Bank", "CHASUS33XXX", "1234567890", "USA", "USD");
        mockFee = RemittanceFee.of("USA", "USD", BigDecimal.valueOf(500), BigDecimal.valueOf(100));
        baseRequest = OverseasRemittanceRequestDto.builder()
            .clientId(clientId)
            .accountId(1L)
            .recipientId(1L)
            .feeId(1L)
            .exchangeRate(BigDecimal.valueOf(1300.0))
            .sendAmount(BigDecimal.valueOf(10_000))
            .build();
    }

    @Test
    @DisplayName("일회성 송금 처리 성공")
    void processRemittance_Success() {
        // given
        // RemittanceProcessorService.execute가 성공적으로 반환할 응답 DTO
        OverseasRemittanceResponseDto mockResponse = OverseasRemittanceResponseDto.builder()
            .remittanceId(1L)
            .recipientName("John Doe")
            .accountNo("1002-123-456789")
            .sendAmount(BigDecimal.valueOf(10_000))
            .remittanceStatus(RemittanceStatus.COMPLETED)
            .build();

        // remittanceProcessorService.execute 호출 시 mockResponse를 반환하도록 설정
        given(remittanceProcessorService.execute(any(ExecuteRemittanceCommand.class))).willReturn(mockResponse);

        // when
        OverseasRemittanceResponseDto result = oneTimeRemittanceService.processRemittance(baseRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRemittanceId()).isEqualTo(1L);
        assertThat(result.getSendAmount()).isEqualTo(BigDecimal.valueOf(10_000));
        assertThat(result.getRecipientName()).isEqualTo("John Doe");
        assertThat(result.getRemittanceStatus()).isEqualTo(RemittanceStatus.COMPLETED);

        // OneTimeRemittanceService가 RemittanceProcessorService.execute를 호출했는지 검증
        verify(remittanceProcessorService).execute(any(ExecuteRemittanceCommand.class));
    }

    @Test
    @DisplayName("일회성 송금 처리 중 RemittanceProcessorService에서 예외 발생 시 실패")
    void processRemittance_Fail_When_Processor_Throws_Exception() {
        // given
        // remittanceProcessorService.execute 호출 시 IllegalArgumentException을 던지도록 설정
        String errorMessage = "계좌를 찾을 수 없습니다.";
        given(remittanceProcessorService.execute(any(ExecuteRemittanceCommand.class)))
            .willThrow(new IllegalArgumentException(errorMessage));

        // when & then
        // OneTimeRemittanceService가 해당 예외를 그대로 던지는지 검증
        assertThatThrownBy(() -> oneTimeRemittanceService.processRemittance(baseRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(errorMessage);

        // OneTimeRemittanceService가 RemittanceProcessorService.execute를 호출했는지 검증
        verify(remittanceProcessorService).execute(any(ExecuteRemittanceCommand.class));
    }
}
