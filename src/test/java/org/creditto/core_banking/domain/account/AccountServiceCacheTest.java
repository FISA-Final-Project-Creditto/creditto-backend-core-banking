package org.creditto.core_banking.domain.account;

import org.creditto.core_banking.domain.account.dto.AccountCreateReq;
import org.creditto.core_banking.domain.account.dto.AccountSummaryRes;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.entity.AccountType;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.account.service.AccountService;
import org.creditto.core_banking.domain.overseasremittance.service.RemittanceProcessorService;
import org.creditto.core_banking.domain.recipient.dto.RecipientCreateDto;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.domain.recipient.repository.RecipientRepository;
import org.creditto.core_banking.global.common.CurrencyCode;
import org.creditto.core_banking.global.util.CacheKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AccountServiceCacheTest {

    @Autowired private AccountService accountService;
    @Autowired private RemittanceProcessorService remittanceProcessorService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private RecipientRepository recipientRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    private final Long testUserId = 1L;
    private String cacheKey;
    private Account account1; // 송금 테스트에 사용할 계좌
    private Recipient testRecipient;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 직접 생성 (Repository 사용)
        String encodedPassword = passwordEncoder.encode("1234");

        account1 = accountRepository.save(Account.of(null, encodedPassword, "Test Account 1", BigDecimal.valueOf(10000), AccountType.SAVINGS, AccountState.ACTIVE, testUserId));
        accountRepository.save(Account.of(null, encodedPassword, "Test Account 2", BigDecimal.valueOf(20000), AccountType.SAVINGS, AccountState.ACTIVE, testUserId));
        accountRepository.save(Account.of(null, encodedPassword, "Test Account 3", BigDecimal.valueOf(30000), AccountType.SAVINGS, AccountState.ACTIVE, testUserId));

        RecipientCreateDto recipientDto = new RecipientCreateDto(
                "Test Recipient", // name
                "01012345678",    // phoneNo
                "+82",            // phoneCc
                "Test Bank",      // bankName
                "ABC",            // bankCode (example)
                "1234567890",     // accountNumber
                "USA",            // country
                CurrencyCode.KRW  // receiveCurrency

        );
        testRecipient = recipientRepository.save(Recipient.of(recipientDto));
        cacheKey = CacheKeyUtil.getTotalBalanceKey(testUserId);
        redisTemplate.delete(cacheKey); // 테스트 시작 전 캐시 비우기

        System.out.println("===== 테스트 준비 완료: 계좌 3개(총 60000원) 생성, 캐시 비움 =====");
    }

        @Test
        @DisplayName("총 잔액 조회 캐싱 및 무효화 전체 시나리오 테스트")
        void testTotalBalanceCachingScenario() {
            // 시나리오 1: 첫 번째 조회 (Cache Miss)
            Object cachedValueBefore = redisTemplate.opsForValue().get(cacheKey);
            assertThat(cachedValueBefore).isNull();

            AccountSummaryRes summary1 = accountService.getTotalBalanceByUserId(testUserId);
            assertThat(summary1.totalBalance()).isEqualByComparingTo(BigDecimal.valueOf(60000));

            Object cachedValueAfterFirstCall = redisTemplate.opsForValue().get(cacheKey);
            assertThat(cachedValueAfterFirstCall).isNotNull();


            // 시나리오 2: 두 번째 조회 (Cache Hit)
            AccountSummaryRes summary2 = accountService.getTotalBalanceByUserId(testUserId);
            assertThat(summary2.totalBalance()).isEqualByComparingTo(BigDecimal.valueOf(60000));


            // 시나리오 3: 신규 계좌 생성으로 인한 캐시 무효화
            accountService.createAccount(new AccountCreateReq("Test Account 4", AccountType.SAVINGS, "1334"), testUserId);

            Object invalidatedCache = redisTemplate.opsForValue().get(cacheKey);
            assertThat(invalidatedCache).isNull();

            // 시나리오 4: 최종 조회
            AccountSummaryRes summary4 = accountService.getTotalBalanceByUserId(testUserId);
            assertThat(summary4.totalBalance()).isEqualByComparingTo(BigDecimal.valueOf(60000));
            assertThat(summary4.accountCount()).isEqualTo(4);

            Object finalCachedValue = redisTemplate.opsForValue().get(cacheKey);
            assertThat(finalCachedValue).isNotNull();
        }}