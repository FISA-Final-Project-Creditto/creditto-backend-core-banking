package org.creditto.core_banking.domain.regularremittance.service;

import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.entity.AccountType;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.entity.Exchange;
import org.creditto.core_banking.domain.exchange.repository.ExchangeRepository;
import org.creditto.core_banking.domain.overseasremittance.dto.ExecuteRemittanceCommand;
import org.creditto.core_banking.domain.overseasremittance.entity.OverseasRemittance;
import org.creditto.core_banking.domain.overseasremittance.repository.OverseasRemittanceRepository;
import org.creditto.core_banking.domain.recipient.dto.RecipientCreateDto;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.domain.regularremittance.dto.*;
import org.creditto.core_banking.domain.regularremittance.entity.*;
import org.creditto.core_banking.domain.regularremittance.repository.RegularRemittanceRepository;
import org.creditto.core_banking.domain.remittancefee.entity.FeeRecord;
import org.creditto.core_banking.domain.remittancefee.repository.FeeRecordRepository;
import org.creditto.core_banking.global.common.CurrencyCode;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RegularRemittanceServiceTest {

    @Autowired
    private RegularRemittanceService regularRemittanceService;
    @Autowired
    private RegularRemittanceRepository regularRemittanceRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private RecipientRepository recipientRepository;
    @Autowired
    private OverseasRemittanceRepository overseasRemittanceRepository;
    @Autowired
    private ExchangeRepository exchangeRepository;
    @Autowired
    private FeeRecordRepository feeRecordRepository;


    private Long testUserId = 3L;
    private Long otherUserId = 4L;
    private Account testAccount;
    private Recipient testRecipient;
    private RegularRemittance testMonthlyRemittance;
    private OverseasRemittance testOverseasRemittance;

    @BeforeEach
    void setup() {
        // 테스트용 계정 생성
        testAccount = accountRepository.save(Account.of("1002456789012", "Test Account", BigDecimal.valueOf(10000000), AccountType.DEPOSIT, AccountState.ACTIVE, testUserId));

        // 테스트용 수취인 생성
        RecipientCreateDto recipientDto = new RecipientCreateDto("Test Recipient", "123456789", "Test Bank", "TEST", "+82", "01012345678", "USA", CurrencyCode.USD);
        testRecipient = recipientRepository.save(Recipient.of(recipientDto));

        // 테스트용 월간 정기송금 생성
        testMonthlyRemittance = regularRemittanceRepository.save(MonthlyRegularRemittance.of(testAccount, testRecipient, CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(1000), 15));

        // 테스트용 해외송금(정기송금과 연결) 내역 생성
        ExchangeReq exchangeReq = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.USD, new BigDecimal("1000"));
        Exchange exchange = exchangeRepository.save(Exchange.of(exchangeReq, new BigDecimal("1300000"), new BigDecimal("1000"), new BigDecimal("1300")));
        FeeRecord feeRecord = feeRecordRepository.save(FeeRecord.create(BigDecimal.ZERO, null, null, null));
        ExecuteRemittanceCommand command = ExecuteRemittanceCommand.of(testUserId, testRecipient.getRecipientId(), testAccount.getId(), testMonthlyRemittance.getRegRemId(), CurrencyCode.KRW, CurrencyCode.USD, new BigDecimal("1000"), LocalDate.now());

        testOverseasRemittance = overseasRemittanceRepository.save(OverseasRemittance.of(testRecipient, testAccount, testMonthlyRemittance, exchange, feeRecord, new BigDecimal("1300000"), command));

        // 다른 사용자를 위한 데이터
        Account otherAccount = accountRepository.save(Account.of("2002456789012", "Other Account", BigDecimal.valueOf(5000000), AccountType.DEPOSIT, AccountState.ACTIVE, otherUserId));
        RecipientCreateDto otherRecipientDto = new RecipientCreateDto("Other Recipient", "987654321", "Other Bank", "OTHR", "+44", "07123456789", "GBR", CurrencyCode.GBP);
        Recipient otherRecipient = recipientRepository.save(Recipient.of(otherRecipientDto));
        regularRemittanceRepository.save(MonthlyRegularRemittance.of(otherAccount, otherRecipient, CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(500), 10));
    }

    @Test
    @Transactional
    @DisplayName("사용자 ID로 정기송금 설정 내역 조회")
    void getScheduledRemittancesByUserId_Success() {
        List<RegularRemittanceResponseDto> result = regularRemittanceService.getScheduledRemittancesByUserId(testUserId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        RegularRemittanceResponseDto found = result.get(0);
        assertThat(found.getSendAmount()).isEqualByComparingTo("1000");
        assertThat(found.getScheduledDate()).isEqualTo(15);
        assertThat(found.getRegRemType()).isEqualTo("MONTHLY");
    }

    @Test
    @Transactional
    @DisplayName("정기송금 ID로 송금 기록 조회")
    void getRegularRemittanceHistoryByRegRemId_Success() {
        List<RemittanceHistoryDto> result = regularRemittanceService.getRegularRemittanceHistoryByRegRemId(testUserId, testMonthlyRemittance.getRegRemId());

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSendAmount()).isEqualByComparingTo("1300000");
    }

    @Test
    @Transactional
    @DisplayName("정기송금 상세 조회 (월간) - 성공")
    void getScheduledRemittanceDetail_Monthly_Success() {
        // when
        RemittanceDetailDto result = regularRemittanceService.getScheduledRemittanceDetail(testUserId, testMonthlyRemittance.getRegRemId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccountNo()).isEqualTo(testAccount.getAccountNo());
        assertThat(result.getSendAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(result.getRegRemType()).isEqualTo("MONTHLY");
        assertThat(result.getScheduledDate()).isEqualTo(15);
        assertThat(result.getScheduledDay()).isNull();
        assertThat(result.getRecipientName()).isEqualTo(testRecipient.getName());
    }

    @Test
    @Transactional
    @DisplayName("정기송금 상세 조회 - 존재하지 않는 송금 ID")
    void getScheduledRemittanceDetail_NotFound() {
        // when & then
        CustomBaseException exception = assertThrows(CustomBaseException.class,
                () -> regularRemittanceService.getScheduledRemittanceDetail(testUserId, 999L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorBaseCode.NOT_FOUND_REGULAR_REMITTANCE);
    }

    @Test
    @Transactional
    @DisplayName("다른 사용자의 송금 기록 조회 시 예외 발생")
    void getRegularRemittanceHistoryByRegRemId_Forbidden() {
        assertThrows(CustomBaseException.class, () -> regularRemittanceService.getRegularRemittanceHistoryByRegRemId(otherUserId, testMonthlyRemittance.getRegRemId()));
    }

    @Test
    @Transactional
    @DisplayName("단일 송금 내역 상세 조회")
    void getRegularRemittanceDetail_Success() {
        RemittanceHistoryDetailDto result = regularRemittanceService.getRemittanceHistoryDetail(testUserId, testOverseasRemittance.getRemittanceId(), testMonthlyRemittance.getRegRemId());

        assertThat(result).isNotNull();
        assertThat(result.getSendAmount()).isEqualByComparingTo("1300000");
        assertThat(result.getRecipientAccountNo()).isEqualTo("123456789");
    }

    @Test
    @Transactional
    @DisplayName("다른 사용자의 단일 송금 내역 상세 조회 시 예외 발생")
    void getRegularRemittanceDetail_Forbidden() {
        assertThrows(CustomBaseException.class, () -> regularRemittanceService.getRemittanceHistoryDetail(otherUserId, testOverseasRemittance.getRemittanceId(), testMonthlyRemittance.getRegRemId()));
    }

    @Test
    @Transactional
    @DisplayName("월간 정기송금 신규 등록")
    void createScheduledRemittance_Monthly_Success() {
        RegularRemittanceCreateDto createDto = new RegularRemittanceCreateDto(
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.JPY, BigDecimal.valueOf(2000), "MONTHLY", 20, null, "New Recipient", "+81", "09012345678", "Tokyo", "JPN", "New Bank", "NEWJ", "987654321"
        );

        RegularRemittanceResponseDto result = regularRemittanceService.createScheduledRemittance(testUserId, createDto);

        assertThat(result).isNotNull();
        assertThat(result.getRegRemType()).isEqualTo("MONTHLY");
        assertThat(result.getSendAmount()).isEqualByComparingTo("2000");
        assertThat(result.getScheduledDate()).isEqualTo(20);
    }

    @Test
    @Transactional
    @DisplayName("주간 정기송금 신규 등록")
    void createScheduledRemittance_Weekly_Success() {
        RegularRemittanceCreateDto createDto = new RegularRemittanceCreateDto(
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.EUR, BigDecimal.valueOf(5000), "WEEKLY", null, DayOfWeek.FRIDAY, "Euro Recipient", "+49", "01712345678", "Berlin", "DEU", "Euro Bank", "EURB", "1122334455"
        );

        RegularRemittanceResponseDto result = regularRemittanceService.createScheduledRemittance(testUserId, createDto);

        assertThat(result).isNotNull();
        assertThat(result.getRegRemType()).isEqualTo("WEEKLY");
        assertThat(result.getSendAmount()).isEqualByComparingTo("5000");
        assertThat(result.getScheduledDay()).isEqualTo(DayOfWeek.FRIDAY);
    }

    @Test
    @Transactional
    @DisplayName("동일한 월간 정기송금 등록 시 예외 발생")
    void createScheduledRemittance_MonthlyDuplicate() {
        RegularRemittanceCreateDto duplicateDto = new RegularRemittanceCreateDto(
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(1000), "MONTHLY", 15, null,
                "Test Recipient", "+82", "01012345678", "Seoul", "USA", "Test Bank", "TEST", "123456789"
        );

        CustomBaseException exception = assertThrows(CustomBaseException.class,
                () -> regularRemittanceService.createScheduledRemittance(testUserId, duplicateDto));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorBaseCode.DUPLICATE_REMITTANCE);
    }

    @Test
    @Transactional
    @DisplayName("동일한 주간 정기송금 등록 시 예외 발생")
    void createScheduledRemittance_WeeklyDuplicate() {
        RegularRemittanceCreateDto weeklyDto = new RegularRemittanceCreateDto(
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.EUR, BigDecimal.valueOf(3000), "WEEKLY", null, DayOfWeek.MONDAY,
                "Weekly Recipient", "+49", "01098765432", "Berlin", "DEU", "Weekly Bank", "WKBK", "9988776655"
        );

        regularRemittanceService.createScheduledRemittance(testUserId, weeklyDto);

        CustomBaseException exception = assertThrows(CustomBaseException.class,
                () -> regularRemittanceService.createScheduledRemittance(testUserId, weeklyDto));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorBaseCode.DUPLICATE_REMITTANCE);
    }


    @Test
    @Transactional
    @DisplayName("정기송금 설정 수정")
    void updateScheduledRemittance_Success() {
        RegularRemittanceUpdateDto updateDto = new RegularRemittanceUpdateDto(
                testAccount.getAccountNo(), BigDecimal.valueOf(1500), RegRemStatus.PAUSED, 25, null
        );

        regularRemittanceService.updateScheduledRemittance(testMonthlyRemittance.getRegRemId(), testUserId, updateDto);

        RegularRemittance updated = regularRemittanceRepository.findById(testMonthlyRemittance.getRegRemId()).get();
        assertThat(updated.getSendAmount()).isEqualByComparingTo("1500");
        assertThat(updated.getRegRemStatus()).isEqualTo(RegRemStatus.PAUSED);
        assertThat(((MonthlyRegularRemittance) updated).getScheduledDate()).isEqualTo(25);
    }

    @Test
    @Transactional
    @DisplayName("다른 사용자의 정기송금 설정 수정 시 예외 발생")
    void updateScheduledRemittance_Forbidden() {
        RegularRemittanceUpdateDto updateDto = new RegularRemittanceUpdateDto(
                testAccount.getAccountNo(), BigDecimal.valueOf(1500), RegRemStatus.PAUSED, 25, null
        );

        assertThrows(CustomBaseException.class, () -> regularRemittanceService.updateScheduledRemittance(testMonthlyRemittance.getRegRemId(), otherUserId, updateDto));
    }

    @Test
    @Transactional
    @DisplayName("정기송금 설정 삭제")
    void deleteScheduledRemittance_Success() {
        Long regRemId = testMonthlyRemittance.getRegRemId();
        regularRemittanceService.deleteScheduledRemittance(regRemId, testUserId);

        Optional<RegularRemittance> deleted = regularRemittanceRepository.findById(regRemId);
        assertThat(deleted).isNotPresent();
    }

    @Test
    @Transactional
    @DisplayName("다른 사용자의 정기송금 설정 삭제 시 예외 발생")
    void deleteScheduledRemittance_Forbidden() {
        Long regRemId = testMonthlyRemittance.getRegRemId();
        assertThrows(CustomBaseException.class, () -> regularRemittanceService.deleteScheduledRemittance(regRemId, otherUserId));
    }
}
