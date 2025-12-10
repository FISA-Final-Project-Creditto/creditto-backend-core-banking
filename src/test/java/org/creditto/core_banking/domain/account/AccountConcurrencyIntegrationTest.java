package org.creditto.core_banking.domain.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.entity.AccountState;
import org.creditto.core_banking.domain.account.entity.AccountType;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.account.service.AccountService;
import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.domain.transaction.repository.TransactionRepository;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class AccountConcurrencyIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private TransactionRepository transactionRepository;

    private RLock mockLock;
    private ReentrantLock localReentrantLock;

    @BeforeEach
    void setUpLock() throws InterruptedException {
        mockLock = Mockito.mock(RLock.class);
        localReentrantLock = new ReentrantLock();
        when(redissonClient.getLock(anyString())).thenReturn(mockLock);
        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenAnswer(invocation -> {
            long wait = invocation.getArgument(0);
            TimeUnit unit = invocation.getArgument(2);
            return localReentrantLock.tryLock(wait, unit);
        });
        when(mockLock.isHeldByCurrentThread()).thenAnswer(invocation -> localReentrantLock.isHeldByCurrentThread());
        Mockito.doAnswer(invocation -> {
            localReentrantLock.unlock();
            return null;
        }).when(mockLock).unlock();
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("동시 출금 시에도 잔액 정합성이 보장된다")
    void concurrentWithdrawalMaintainsConsistency() throws InterruptedException {
        Account prepared = Account.of(
                null,
                "encoded-password",
                "테스트 계좌",
                new BigDecimal("100000"),
                AccountType.DEPOSIT,
                AccountState.ACTIVE,
                1L
        );

        Account saved = accountRepository.save(prepared);

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger insufficientCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        Runnable withdrawTask = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                accountService.processTransaction(saved.getId(), new BigDecimal("70000"), TxnType.WITHDRAWAL, null);
                successCount.incrementAndGet();
            } catch (CustomBaseException e) {
                if (e.getErrorCode() == ErrorBaseCode.INSUFFICIENT_FUNDS) {
                    insufficientCount.incrementAndGet();
                } else if (e.getErrorCode() == ErrorBaseCode.TRANSACTION_FAILED) {
                    failedCount.incrementAndGet();
                } else {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(withdrawTask);
        }

        readyLatch.await(3, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(5, TimeUnit.SECONDS);
        executorService.shutdownNow();

        Account reloaded = accountRepository.findById(saved.getId())
                .orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failedCount.get() + insufficientCount.get()).isEqualTo(1);
        assertThat(reloaded.getBalance()).isEqualByComparingTo(new BigDecimal("30000"));
    }

    @Test
    @DisplayName("100건의 동시 출금 시도 시 100건의 출금이 정확히 완료된다")
    void hundredConcurrentWithdrawalsCompleteSuccessfully() throws InterruptedException {
        Account prepared = Account.of(
                null,
                "encoded-password",
                "테스트 계좌",
                new BigDecimal("1000000"),
                AccountType.DEPOSIT,
                AccountState.ACTIVE,
                1L
        );

        Account saved = accountRepository.save(prepared);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        BigDecimal withdrawAmount = new BigDecimal("10000");

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        Runnable withdrawTask = () -> {
            try {
                readyLatch.countDown();
                startLatch.await();
                boolean completed = false;
                while (!completed && successCount.get() < threadCount) {
                    try {
                        accountService.processTransaction(saved.getId(), withdrawAmount, TxnType.WITHDRAWAL, null);
                        int current = successCount.incrementAndGet();
                        assertThat(current).isLessThanOrEqualTo(threadCount);
                        completed = true;
                    } catch (CustomBaseException e) {
                        if (e.getErrorCode() == ErrorBaseCode.ACCOUNT_LOCK_TIMEOUT && successCount.get() < threadCount) {
                            continue;
                        }

                        failureCount.incrementAndGet();
                        completed = true;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                doneLatch.countDown();
            }
        };

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(withdrawTask);
        }

        readyLatch.await(3, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(15, TimeUnit.SECONDS);
        executorService.shutdownNow();

        Account reloaded = accountRepository.findById(saved.getId())
                .orElseThrow();

        System.out.println("송금 횟수 : " + successCount);
        System.out.println("송금 실패 횟수 : " + failureCount);
        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(failureCount.get()).isZero();
        assertThat(reloaded.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @TestConfiguration
    static class MockRedissonConfiguration {

        @Bean
        @Primary
        public RedissonClient mockRedissonClient() {
            return Mockito.mock(RedissonClient.class);
        }
    }
}
