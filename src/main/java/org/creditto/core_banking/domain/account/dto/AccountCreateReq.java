package org.creditto.core_banking.domain.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.creditto.core_banking.domain.account.entity.AccountType;


public record AccountCreateReq(
        String accountName,
        AccountType accountType,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = "^\\d{4}$", message = "비밀번호는 4자리 숫자여야 합니다.")
        String password,
        String passwordConfirmation
) {
}