package org.creditto.core_banking.domain.recipient.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;
import org.creditto.core_banking.global.common.CurrencyCode;

@Entity
@Getter
@Builder
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
    private CurrencyCode currencyCode;

    public static Recipient of(
            String name,
            String phoneNo,
            String phoneCc,
            String bankName,
            String bankCode,
            String accountNo,
            String country,
            CurrencyCode currencyCode
    ){
        return Recipient.builder()
                .name(name)
                .phoneNo(phoneNo)
                .phoneCc(phoneCc)
                .bankName(bankName)
                .bankCode(bankCode)
                .accountNo(accountNo)
                .country(country)
                .currencyCode(currencyCode)
                .build();
    }
}
