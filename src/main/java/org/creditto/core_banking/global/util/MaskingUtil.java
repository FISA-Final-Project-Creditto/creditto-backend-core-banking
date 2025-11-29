package org.creditto.core_banking.global.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MaskingUtil {

    private static final List<String> ACCOUNT_NUMBER_FIELD_NAMES = List.of(
            "accountNumber",
            "accountNo",
            "fromAccountNumber",
            "toAccountNumber",
            "senderAccountNumber",
            "receiverAccountNumber"
    );


    public static String maskSensitiveData(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }

        String fieldNamesPattern = ACCOUNT_NUMBER_FIELD_NAMES.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

        Pattern stringPattern = Pattern.compile("(\"(?:" + fieldNamesPattern + ")\"\\s*:\\s*\")(.*?)(\")", Pattern.CASE_INSENSITIVE);
        String masked = replaceWithMask(raw, stringPattern, true);

        Pattern numericPattern = Pattern.compile("(\"(?:" + fieldNamesPattern + ")\"\\s*:\\s*)(\\d+)", Pattern.CASE_INSENSITIVE);
        return replaceWithMask(masked, numericPattern, false);
    }

    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return accountNumber;
        }

        long digitCount = accountNumber.chars()
                .filter(Character::isDigit)
                .count();

        if (digitCount == 0) {
            return accountNumber;
        }

        int head = (int) Math.min(3, digitCount);
        int tail = digitCount > head ? (int) Math.min(2, digitCount - head) : 0;
        long maskableDigits = digitCount - head - tail;

        if (maskableDigits <= 0) {
            return accountNumber.replaceAll("\\d", "*");
        }

        StringBuilder builder = new StringBuilder();
        long digitIndex = 0;

        for (char ch : accountNumber.toCharArray()) {
            if (!Character.isDigit(ch)) {
                builder.append(ch);
                continue;
            }

            digitIndex++;
            if (digitIndex <= head || digitIndex > digitCount - tail) {
                builder.append(ch);
                continue;
            }

            builder.append('*');
        }

        return builder.toString();
    }

    private static String replaceWithMask(String value, Pattern pattern, boolean hasQuotes) {
        Matcher matcher = pattern.matcher(value);
        StringBuffer sb = new StringBuffer();
        boolean replaced = false;

        while (matcher.find()) {
            replaced = true;
            String accountNumber = matcher.group(2);
            String maskedNumber = maskAccountNumber(accountNumber);
            String replacement = hasQuotes
                    ? matcher.group(1) + maskedNumber + matcher.group(3)
                    : matcher.group(1) + maskedNumber;
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }

        if (!replaced) {
            return value;
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
