package org.creditto.core_banking.global.util;

public final class CacheKeyUtil {

    private static final String TOTAL_BALANCE_PREFIX = "totalBalance::";

    private CacheKeyUtil() {
    }

    public static String getTotalBalanceKey(Long userId) {
        return TOTAL_BALANCE_PREFIX + userId;
    }
}
