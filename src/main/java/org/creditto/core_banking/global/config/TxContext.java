package org.creditto.core_banking.global.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TxContext {
    private static final ThreadLocal<String> context = new ThreadLocal<>();

    public static void start() {
        context.set(UUID.randomUUID().toString());
    }

    public static String get() {
        return context.get();
    }

    public static void clear() {
        context.remove();
    }
}
