package org.creditto.core_banking.domain.recipient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.creditto.core_banking.global.common.CurrencyCode;

public record RecipientCreateDto(
        @NotBlank(message = "수취인 이름은 필수입니다.")
        String name,

        @NotBlank(message = "수취인 계좌번호는 필수입니다.")
        String accountNumber,

        @NotBlank(message = "수취인 은행 이름은 필수입니다.")
        String bankName,

        @NotBlank(message = "수취인 은행코드는 필수입니다.")
        String bankCode,

        @NotBlank(message = "수취인 전화 국가번호는 필수입니다.")
        String phoneCc,

        @NotBlank(message = "수취인 연락처는 필수입니다.")
        String phoneNo,

        @NotBlank(message = "수취인 국가는 필수입니다.")
        String country,

        @NotNull(message = "수취 통화는 필수입니다.")
        CurrencyCode receiveCurrency
) {
}
