package org.creditto.core_banking.domain.overseasremittance.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.recipient.dto.RecipientCreateDto;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 클라이언트로부터 해외송금(일회성, 정기) 요청을 받기 위한 DTO(Data Transfer Object)입니다.
 * 내부 식별자(ID) 대신, 계좌 번호, 수취인 상세 정보 등 비즈니스적인 의미를 갖는 원시 데이터를 직접 받습니다.
 * 이를 통해 외부 서비스와 코어 뱅킹 시스템 간의 결합도를 낮춥니다.
 */
@Getter
@Builder
@AllArgsConstructor
public class OverseasRemittanceRequestDto {

    /**
     * 출금될 계좌의 번호
     */
    @NotBlank(message = "출금 계좌번호는 필수입니다.")
    private String accountNo;

    /**
     * 출금될 계좌의 비밀번호
     */
    @NotBlank(message = "계좌 비밀번호는 필수입니다.")
    @Pattern(regexp = "^\\d{4}$", message = "비밀번호는 4자리 숫자여야 합니다.")
    private String password;

    /**
     * 수취인의 상세 정보
     */
    @Valid
    @NotNull(message = "수취인 정보는 필수입니다.")
    private RecipientInfo recipientInfo;

    /**
     * 정기송금인 경우의 정기송금 식별자 (일회성 송금의 경우 null)
     */
    private Long recurId;

    /**
     * 송금 시작일 (정기송금의 경우 사용)
     */
    private LocalDate startDate;

    /**
     * 보내는 통화 (e.g. "KRW")
     */
    @NotNull(message = "송금 통화는 필수입니다.")
    private CurrencyCode sendCurrency;

    /**
     * 보내는 금액 (수취 통화 기준)
     */
    @NotNull(message = "송금액은 필수입니다.")
    @Positive(message = "송금액은 0보다 커야 합니다.")
    private BigDecimal targetAmount;

    /**
     * 해외송금 수취인의 상세 정보를 담는 내부 클래스입니다.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecipientInfo {
        /**
         * 수취인 이름
         */
        @NotBlank(message = "수취인 이름은 필수입니다.")
        private String name;

        /**
         * 수취인의 계좌번호
         */
        @NotBlank(message = "수취인 계좌번호는 필수입니다.")
        private String accountNo;

        /**
         * 수취인 은행의 이름
         */
        @NotBlank(message = "수취인 은행 이름은 필수입니다.")
        private String bankName;

        /**
         * 수취인 은행의 식별 코드 (e.g. SWIFT Code)
         */
        @NotBlank(message = "수취인 은행코드는 필수입니다.")
        private String bankCode;

        /**
         * 수취인 전화 국가번호
         */
        @NotBlank(message = "수취인 전화 국가번호는 필수입니다.")
        private String phoneCc;

        /**
         * 수취인 연락처
         */
        @NotBlank(message = "수취인 연락처는 필수입니다.")
        private String phoneNo;

        /**
         * 수취인 거주 국가 ("USA")
         */
        @NotBlank(message = "수취인 국가는 필수입니다.")
        private String country;

        /**
         * 받는 통화 (e.g. "USD")
         */
        @NotNull(message = "수취 통화는 필수입니다.")
        private CurrencyCode receiveCurrency;

        public RecipientCreateDto toRecipientCreateDto() {
            return new RecipientCreateDto(
                this.name,
                this.accountNo,
                this.bankName,
                this.bankCode,
                this.phoneCc,
                this.phoneNo,
                this.country,
                this.receiveCurrency
            );
        }
    }
}
