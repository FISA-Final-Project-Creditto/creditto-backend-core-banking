package org.creditto.core_banking.domain.account.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "core.account-lock")
public class AccountLockProperties {

    private final long waitMillis;
    private final long leaseMillis;
    private final String accountLockPrefix;

    public AccountLockProperties(long waitMillis, long leaseMillis, String accountLockPrefix) {
        this.waitMillis = waitMillis;
        this.leaseMillis = leaseMillis;
        this.accountLockPrefix = accountLockPrefix;
    }

    public long getWaitMillis() {
        return waitMillis;
    }

    public long getLeaseMillis() {
        return leaseMillis;
    }

    public String getAccountLockPrefix() {
        return accountLockPrefix;
    }
}
