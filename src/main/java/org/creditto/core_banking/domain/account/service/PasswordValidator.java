package org.creditto.core_banking.domain.account.service;

import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {

    public void validatePassword(String password) {
        if (isRepeatingNum(password)) {
            throw new CustomBaseException(ErrorBaseCode.PASSWORD_REPEATING_DIGITS);
        }
        if (isSequentialNum(password)) {
            throw new CustomBaseException(ErrorBaseCode.PASSWORD_SEQUENTIAL_DIGIS);
        }
    }

    // "1111", "2222"와 같이 모든 숫자가 동일한 숫자인지 확인
    private boolean isRepeatingNum(String password) {
        char first = password.charAt(0);
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }


    // "1234", "4321"과 같이 오름차순/내림차순으로 연속되는 숫자인지 확인
    private boolean isSequentialNum(String password) {
        boolean asc = true;
        boolean desc = true;

        for (int i = 0; i < password.length() - 1; i++) {
            int curr = Character.getNumericValue(password.charAt(i)); // char -> int
            int next = Character.getNumericValue(password.charAt(i + 1));

            if (next != curr + 1) {
                asc = false;
            }
            if (next != curr -1) {
                desc = false;
            }
        }
        return asc || desc;
    }
}
