package org.creditto.core_banking.domain.overseasremittance;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceRequestDto;
import org.creditto.core_banking.domain.overseasremittance.dto.OverseasRemittanceResponseDto;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.domain.overseasremittance.service.OverseasRemittanceService;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.domain.regularremittance.repository.RegularRemittanceRepository;
import org.creditto.core_banking.domain.remittancefee.entity.RemittanceFee;
import org.creditto.core_banking.domain.remittancefee.repository.RemittanceFeeRepository;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.creditto.core_banking.domain.account.entity.AccountState.ACTIVE;
import static org.creditto.core_banking.domain.account.entity.AccountType.DEPOSIT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OverseasRemittanceServiceImplTest {

    @Mock
    private OverseasRemittanceRepository overseasRemittanceRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RemittanceFeeRepository remittanceFeeRepository;
    @Mock
    private RecipientRepository recipientRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private RegularRemittanceRepository regularRemittanceRepository;

    @InjectMocks
    private OverseasRemittanceService overseasRemittanceService;

    // 공통 목 객체
    private String clientId;
    private Account mockAccount;
    private Recipient mockRecipient;
    private RemittanceFee mockFee;
    private OverseasRemittanceRequestDto baseRequest;

    // 각 테스트 실행 전에 항상 호출
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
    @DisplayName("해외송금 내역 조회 성공")
    void getRemittanceList_Success() {
        // given
        // setUp()에서 생성된 공통 객체 사용
        OverseasRemittance mockRemittance1 = OverseasRemittance.of(mockRecipient, mockAccount, clientId, mockFee, null, BigDecimal.valueOf(1300.0), BigDecimal.valueOf(5_000), null);
        OverseasRemittance mockRemittance2 = OverseasRemittance.of(mockRecipient, mockAccount, clientId, mockFee, null, BigDecimal.valueOf(1310.0), BigDecimal.valueOf(6_000), null);
        OverseasRemittance mockRemittance3 = OverseasRemittance.of(mockRecipient, mockAccount, clientId, mockFee, null, BigDecimal.valueOf(1320.0), BigDecimal.valueOf(7_000), null);

        given(overseasRemittanceRepository.findByClientIdWithDetails(clientId)).willReturn(List.of(mockRemittance1, mockRemittance2, mockRemittance3));

        // when
        List<OverseasRemittanceResponseDto> result = overseasRemittanceService.getRemittanceList(clientId);

        // then
        assertThat(result).hasSize(3);
             assertThat(result.get(0).getSendAmount()).isEqualTo(BigDecimal.valueOf(5_000));
             assertThat(result.get(1).getSendAmount()).isEqualTo(BigDecimal.valueOf(6_000));
             assertThat(result.get(2).getSendAmount()).isEqualTo(BigDecimal.valueOf(7_000));
    }

    @Test
    @DisplayName("해외송금 처리 성공")
    void processRemittance_Success() {
        // given
        // setUp()에서 생성된 공통 객체 사용
        given(accountRepository.findById(1L)).willReturn(Optional.of(mockAccount));
        given(recipientRepository.findById(1L)).willReturn(Optional.of(mockRecipient));
        given(remittanceFeeRepository.findById(1L)).willReturn(Optional.of(mockFee));
        given(overseasRemittanceRepository.save(any(OverseasRemittance.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OverseasRemittanceResponseDto result = overseasRemittanceService.processRemittance(baseRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSendAmount()).isEqualTo(BigDecimal.valueOf(10_000));
        assertThat(result.getRecipientName()).isEqualTo("John Doe");

        // 계좌 정보, 트랜잭션(수수료/출금 -> 총 2회), 해외송금 내역이 저장되었는지 검증
        verify(accountRepository).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(overseasRemittanceRepository).save(any(OverseasRemittance.class));

        // Initial balance 600_000 - totalFee 600 - sendAmount 10_000 = 589_400
        assertThat(mockAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(589_400));
    }

    @Test
    @DisplayName("잔액 부족으로 송금 실패")
    void processRemittance_Fail_InsufficientBalance() {
        // given
        // 계좌 잔액 500원
        Account insufficientAccount = Account.of("1002-123-456789", "예금계좌", BigDecimal.valueOf(500), DEPOSIT , ACTIVE, clientId);
        given(accountRepository.findById(1L)).willReturn(Optional.of(insufficientAccount));
        given(recipientRepository.findById(1L)).willReturn(Optional.of(mockRecipient));
        given(remittanceFeeRepository.findById(1L)).willReturn(Optional.of(mockFee));

        // when & then
        assertThatThrownBy(() -> overseasRemittanceService.processRemittance(baseRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("잔액이 부족합니다.");
    }

    @Test
    @DisplayName("계좌를 찾을 수 없어 송금 실패")
    void processRemittance_Fail_AccountNotFound() {
        // given
        // ID가 1인 계좌가 DB에 존재x
        given(accountRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> overseasRemittanceService.processRemittance(baseRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage());
    }
}
