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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

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
    private ExchangeRepository exchangeRepository;
    @Autowired
    private FeeRecordRepository feeRecordRepository;

    @Autowired
    private OverseasRemittanceRepository overseasRemittanceRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    private Long testUserId = 3L;
    private Long otherUserId = 4L;
    private Account testAccount;
    private Recipient testRecipient;
    private RegularRemittance testMonthlyRemittance;
    private RegularRemittance testWeeklyRemittance;
    private OverseasRemittance testOverseasRemittance;
    private LocalDate startedAt = LocalDate.of(2023, 1, 1);

    @BeforeEach
    void setup() {
        // 테스트용 계정 생성
        testAccount = accountRepository.save(Account.of("1002456789012", "1058", "Test Account", BigDecimal.valueOf(10000000), AccountType.DEPOSIT, AccountState.ACTIVE, testUserId));

        // 테스트용 수취인 생성
        RecipientCreateDto recipientDto = new RecipientCreateDto("Test Recipient", "123456789", "Test Bank", "TEST", "+82", "01012345678", "USA", CurrencyCode.USD);
        testRecipient = recipientRepository.save(Recipient.of(recipientDto));

        // 테스트용 월간 정기송금 생성
        testMonthlyRemittance = regularRemittanceRepository.save(MonthlyRegularRemittance.of(testAccount, testRecipient, CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(1000), 15, startedAt));

        // 테스트용 주간 정기송금 생성
        testWeeklyRemittance = regularRemittanceRepository.save(WeeklyRegularRemittance.of(testAccount, testRecipient, CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(500), DayOfWeek.WEDNESDAY, startedAt));

        // 테스트용 해외송금(정기송금과 연결) 내역 생성
        ExchangeReq exchangeReq = new ExchangeReq(CurrencyCode.KRW, CurrencyCode.USD, new BigDecimal("1000"));
        Exchange exchange = exchangeRepository.save(Exchange.of(exchangeReq, new BigDecimal("1300000"), new BigDecimal("1000"), new BigDecimal("1300")));
        FeeRecord feeRecord = feeRecordRepository.save(FeeRecord.create(BigDecimal.ZERO, null, null, null));
        ExecuteRemittanceCommand command = ExecuteRemittanceCommand.of(testUserId, testRecipient.getRecipientId(), testAccount.getId(), testMonthlyRemittance.getRegRemId(), CurrencyCode.KRW, CurrencyCode.USD, new BigDecimal("1000"), LocalDate.now());

        testOverseasRemittance = overseasRemittanceRepository.save(OverseasRemittance.of(testRecipient, testAccount, testMonthlyRemittance, exchange, feeRecord, new BigDecimal("1300000"), command));

        // 다른 사용자를 위한 데이터
        Account otherAccount = accountRepository.save(Account.of("2002456789012", "1058", "Other Account", BigDecimal.valueOf(5000000), AccountType.DEPOSIT, AccountState.ACTIVE, otherUserId));
        RecipientCreateDto otherRecipientDto = new RecipientCreateDto("Other Recipient", "987654321", "Other Bank", "OTHR", "+44", "07123456789", "GBR", CurrencyCode.GBP);
        Recipient otherRecipient = recipientRepository.save(Recipient.of(otherRecipientDto));
        regularRemittanceRepository.save(MonthlyRegularRemittance.of(otherAccount, otherRecipient, CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(500), 10, startedAt));
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 후 Redis 데이터 삭제하여 격리성 보장
        Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushDb();
    }

    @Test
    @Transactional
    @DisplayName("사용자 ID로 정기송금 설정 내역 조회")
    void getScheduledRemittancesByUserId_Success() {
        List<RegularRemittanceResponseDto> result = regularRemittanceService.getScheduledRemittancesByUserId(testUserId);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
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
    @DisplayName("다른 사용자의 송금 기록 조회 시 예외 발생")
    void getRegularRemittanceHistoryByRegRemId_Forbidden() {
        assertThrows(CustomBaseException.class, () -> regularRemittanceService.getRegularRemittanceHistoryByRegRemId(otherUserId, testMonthlyRemittance.getRegRemId()));
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
        assertThat(result.getStartedAt()).isEqualTo(startedAt);
    }

    @Test
    @Transactional
    @DisplayName("정기송금 상세 조회 (주간) - 성공")
    void getScheduledRemittanceDetail_Weekly_Success() {
        // when
        RemittanceDetailDto result = regularRemittanceService.getScheduledRemittanceDetail(testUserId, testWeeklyRemittance.getRegRemId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getAccountNo()).isEqualTo(testAccount.getAccountNo());
        assertThat(result.getSendAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(result.getRegRemType()).isEqualTo("WEEKLY");
        assertThat(result.getScheduledDay()).isEqualTo(DayOfWeek.WEDNESDAY);
        assertThat(result.getScheduledDate()).isNull();
        assertThat(result.getRecipientName()).isEqualTo(testRecipient.getName());
        assertThat(result.getStartedAt()).isEqualTo(startedAt);
    }

    @Test
    @Transactional
    @DisplayName("정기송금 상세 조회 - 다른 사용자 접근 시 예외 발생")
    void getScheduledRemittanceDetail_Forbidden() {
        // when & then
        CustomBaseException exception = assertThrows(CustomBaseException.class,
                () -> regularRemittanceService.getScheduledRemittanceDetail(otherUserId, testMonthlyRemittance.getRegRemId()));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorBaseCode.FORBIDDEN);
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
        LocalDate newStartedAt = LocalDate.of(2025, 1, 1);
        RegularRemittanceCreateDto createDto = new RegularRemittanceCreateDto(
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.JPY, BigDecimal.valueOf(2000), "MONTHLY", 20, null, newStartedAt, "New Recipient", "+81", "09012345678", "Tokyo", "JPN", "New Bank", "NEWJ", "987654321"
        );

        RegularRemittanceResponseDto result = regularRemittanceService.createScheduledRemittance(testUserId, createDto);

        assertThat(result).isNotNull();
        assertThat(result.getRegRemType()).isEqualTo("MONTHLY");
        assertThat(result.getSendAmount()).isEqualByComparingTo("2000");
        assertThat(result.getScheduledDate()).isEqualTo(20);
        assertThat(result.getStartedAt()).isEqualTo(newStartedAt);
    }

    @Test
    @Transactional
    @DisplayName("주간 정기송금 신규 등록")
    void createScheduledRemittance_Weekly_Success() {
        LocalDate newStartedAt = LocalDate.of(2025, 1, 1);
        RegularRemittanceCreateDto createDto = new RegularRemittanceCreateDto(
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.EUR, BigDecimal.valueOf(5000), "WEEKLY", null, DayOfWeek.FRIDAY, newStartedAt, "Euro Recipient", "+49", "01712345678", "Berlin", "DEU", "Euro Bank", "EURB", "1122334455"
        );

        RegularRemittanceResponseDto result = regularRemittanceService.createScheduledRemittance(testUserId, createDto);

        assertThat(result).isNotNull();
        assertThat(result.getRegRemType()).isEqualTo("WEEKLY");
        assertThat(result.getSendAmount()).isEqualByComparingTo("5000");
        assertThat(result.getScheduledDay()).isEqualTo(DayOfWeek.FRIDAY);
        assertThat(result.getStartedAt()).isEqualTo(newStartedAt);
    }

    @Test
    @Transactional
    @DisplayName("동일한 월간 정기송금 등록 시 예외 발생")
    void createScheduledRemittance_MonthlyDuplicate() {
        RegularRemittanceCreateDto duplicateDto = new RegularRemittanceCreateDto(
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(1000), "MONTHLY", 15, null, startedAt,
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
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(500), "WEEKLY", null, DayOfWeek.WEDNESDAY, startedAt,
                "Weekly Recipient", "+49", "01098765432", "Berlin", "DEU", "Weekly Bank", "WKBK", "9988776655"
        );

        regularRemittanceService.createScheduledRemittance(testUserId, weeklyDto);

        CustomBaseException exception = assertThrows(CustomBaseException.class,
                () -> regularRemittanceService.createScheduledRemittance(testUserId, weeklyDto));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorBaseCode.DUPLICATE_REMITTANCE);
    }


    @Test
    @Transactional
    @DisplayName("정기송금 설정 수정 시 캐시 삭제")
    void updateScheduledRemittance_Success() {
        // given
        Long regRemId = testMonthlyRemittance.getRegRemId();
        String key = "regularRemittanceHistory::" + regRemId;
        redisTemplate.opsForValue().set(key, List.of()); // 미리 캐시를 저장해둠

        RegularRemittanceUpdateDto updateDto = new RegularRemittanceUpdateDto(
                testAccount.getAccountNo(), BigDecimal.valueOf(1500), RegRemStatus.PAUSED, 25, null
        );

        // when
        regularRemittanceService.updateScheduledRemittance(regRemId, testUserId, updateDto);

        // then
        RegularRemittance updated = regularRemittanceRepository.findById(regRemId).get();
        assertThat(updated.getSendAmount()).isEqualByComparingTo("1500");
        assertThat(updated.getRegRemStatus()).isEqualTo(RegRemStatus.PAUSED);
        assertThat(((MonthlyRegularRemittance) updated).getScheduledDate()).isEqualTo(25);

        // 캐시 삭제 검증
        Object cachedValue = redisTemplate.opsForValue().get(key);
        assertThat(cachedValue).isNull();
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
    @DisplayName("정기송금 설정 삭제 시 캐시 삭제")
    void deleteScheduledRemittance_Success() {
        // given
        Long regRemId = testMonthlyRemittance.getRegRemId();
        String key = "regularRemittanceHistory::" + regRemId;
        redisTemplate.opsForValue().set(key, List.of()); // 미리 캐시를 저장해둠

        // when
        regularRemittanceService.deleteScheduledRemittance(regRemId, testUserId);

        // then
        Optional<RegularRemittance> deleted = regularRemittanceRepository.findById(regRemId);
        assertThat(deleted).isNotPresent();

        // 캐시 삭제 검증
        Object cachedValue = redisTemplate.opsForValue().get(key);
        assertThat(cachedValue).isNull();
    }

    @Test
    @Transactional
    @DisplayName("다른 사용자의 정기송금 설정 삭제 시 예외 발생")
    void deleteScheduledRemittance_Forbidden() {
        Long regRemId = testMonthlyRemittance.getRegRemId();
        assertThrows(CustomBaseException.class, () -> regularRemittanceService.deleteScheduledRemittance(regRemId, otherUserId));
    }

    @Test
    @Transactional
    @DisplayName("정기송금 생성 시 startedAt 필드 영속성 확인")
    void createScheduledRemittance_PersistsStartedAt() {
        LocalDate expectedStartedAt = LocalDate.of(2024, 6, 1);
        RegularRemittanceCreateDto createDto = new RegularRemittanceCreateDto(
                testAccount.getAccountNo(), CurrencyCode.KRW, CurrencyCode.USD, BigDecimal.valueOf(3000), "MONTHLY", 10, null, expectedStartedAt, "Persistent Recipient", "+1", "1234567890", "New York", "USA", "Citi", "CITI", "9876543210"
        );

        RegularRemittanceResponseDto result = regularRemittanceService.createScheduledRemittance(testUserId, createDto);

        RegularRemittance savedRemittance = regularRemittanceRepository.findById(result.getRegRemId())
                .orElseThrow(() -> new AssertionError("Saved remittance not found"));

        assertThat(savedRemittance.getStartedAt()).isEqualTo(expectedStartedAt);
    }
    
    // --- 캐싱 관련 새로운 테스트 ---

    @Test
    @Transactional
    @DisplayName("정기송금 내역 조회 - Cache Miss (캐시 없음)")
    void getRegularRemittanceHistoryByRegRemId_CacheMiss() {
        // given
        Long regRemId = testMonthlyRemittance.getRegRemId();
        String key = "regularRemittanceHistory::" + regRemId;

        // when
        regularRemittanceService.getRegularRemittanceHistoryByRegRemId(testUserId, regRemId);

        // then
        // 1. DB를 조회했는지 검증 (SpyBean)
        verify(overseasRemittanceRepository, times(1)).findByRecur_RegRemIdOrderByCreatedAtDesc(regRemId);
        // 2. Redis에 값이 저장되었는지 검증
        Object cachedValue = redisTemplate.opsForValue().get(key);
        assertThat(cachedValue).isNotNull();
        assertThat((List<RemittanceHistoryDto>) cachedValue).hasSize(1);
    }

    @Test
    @Transactional
    @DisplayName("정기송금 내역 조회 - Cache Hit (캐시 있음)")
    void getRegularRemittanceHistoryByRegRemId_CacheHit() {
        // given
        Long regRemId = testMonthlyRemittance.getRegRemId();
        String key = "regularRemittanceHistory::" + regRemId;
        
        // 미리 Redis에 가짜 데이터 저장
        List<RemittanceHistoryDto> fakeCachedList = List.of(new RemittanceHistoryDto(999L, BigDecimal.TEN, BigDecimal.ONE, LocalDate.now()));
        redisTemplate.opsForValue().set(key, fakeCachedList);
        
        // when
        List<RemittanceHistoryDto> result = regularRemittanceService.getRegularRemittanceHistoryByRegRemId(testUserId, regRemId);

        // then
        // 1. 반환된 결과가 캐시된 데이터와 동일한지 검증 (DB 데이터가 아님을 증명)
        assertThat(result.get(0).getRemittanceId()).isEqualTo(999L);
        
        // 2. DB를 조회하지 않았는지 검증 (SpyBean)
        verify(overseasRemittanceRepository, never()).findByRecur_RegRemIdOrderByCreatedAtDesc(anyLong());
    }
}
