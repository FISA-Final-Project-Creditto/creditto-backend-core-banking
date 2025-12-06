package org.creditto.core_banking.domain.account.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountLockService {

    private static final String ACCOUNT_LOCK_PREFIX = "lock:account:";
    private static final long LOCK_WAIT_MILLIS = 3000L;
    private static final long LOCK_LEASE_MILLIS = 15000L;

    private final RedissonClient redissonClient;

    public <T> T executeWithLock(Long accountId, LockCallback<T> callback) {
        RLock lock = redissonClient.getLock(ACCOUNT_LOCK_PREFIX + accountId);
        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_MILLIS, LOCK_LEASE_MILLIS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new CustomBaseException(ErrorBaseCode.ACCOUNT_LOCK_TIMEOUT);
            }
            return callback.invoke();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomBaseException(ErrorBaseCode.ACCOUNT_LOCK_INTERRUPTED);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void executeWithLock(Long accountId, Runnable runnable) {
        executeWithLock(accountId, () -> {
            runnable.run();
            return null;
        });
    }

    @FunctionalInterface
    public interface LockCallback<T> {
        T invoke();
    }
}
