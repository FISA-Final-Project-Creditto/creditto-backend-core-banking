package org.creditto.core_banking.domain.recipient.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.creditto.core_banking.domain.recipient.dto.RecipientCreateDto;
import org.creditto.core_banking.global.common.BaseEntity;
import org.creditto.core_banking.global.common.CurrencyCode;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"bankCode", "accountNo", "name"})
})
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipient extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recipientId;

    private String name;

    private String phoneNo;

    // phoneCountryCode
    // ex)+82
    @Size(max = 4)
    private String phoneCc;

    private String bankName;

    // 일반적으로 8자리 또는 11자리의 문자 및 숫자
    private String bankCode;

    private String accountNo;

    private String country;

    // ex)KRW
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyCode;

    public static Recipient of(RecipientCreateDto dto) {
        return Recipient.builder()
                .name(dto.name())
                .phoneNo(dto.phoneNo())
                .phoneCc(dto.phoneCc())
                .bankName(dto.bankName())
                .bankCode(dto.bankCode())
                .accountNo(dto.accountNo())
                .country(dto.country())
                .currencyCode(dto.receiveCurrency())
                .build();
    }

    public void updateDetails(
            String phoneNo,
            String bankName,
            String bankCode,
            String accountNo
    ) {
        this.phoneNo = phoneNo;
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.accountNo = accountNo;
    }
}
