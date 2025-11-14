package org.creditto.core_banking.domain.overseasremittance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class OverseasRemittanceRequestDto {

    @NotBlank(message = "고객 ID는 필수입니다.")
    private String clientId;      // 고객 ID

    @NotNull(message = "수취인 ID는 필수입니다.")
    private Long recipientId;     // 수취인 ID

    @NotNull(message = "출금계좌 ID는 필수입니다.")
    private Long accountId;       // 출금계좌 ID

    @NotNull(message = "수수료 정책 ID는 필수입니다.")
    private Long feeId;           // 수수료 정책 ID

    private Long regRemId;        // 정기송금 ID (선택)

    @NotNull(message = "환율은 필수입니다.")
    @Positive(message = "환율은 0보다 커야 합니다.")
    private BigDecimal exchangeRate;  // 환율

    @NotBlank(message = "통화코드는 필수입니다.")
    private String currencyCode;       // 통화코드

    @NotNull(message = "송금액은 필수입니다.")
    @Positive(message = "송금액은 0보다 커야 합니다.")
    private BigDecimal sendAmount;     // 송금금액

    private BigDecimal receivedAmount; // 수취금액 (요청 시에는 사용되지 않음)
}
