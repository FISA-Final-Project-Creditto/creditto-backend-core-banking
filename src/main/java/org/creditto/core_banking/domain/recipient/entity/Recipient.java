package org.creditto.core_banking.domain.recipient.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;

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
    @Size(min = 3, max = 3)
    private String currencyCode;

    public static Recipient of(
            String name,
            String phoneNo,
            String phoneCc,
            String bankName,
            String bankCode,
            String accountNo,
            String country,
            String currencyCode
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
