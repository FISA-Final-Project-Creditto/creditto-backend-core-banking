package org.creditto.core_banking.domain.remittanceFee.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)

public class RemittanceFee extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feeId;

    private String country;

    private String currency;

    private BigDecimal baseFee;

    //송금 금액에 따른 비율 수수료
    private BigDecimal variableFee;

    @CreationTimestamp
    private LocalDate effectiveDate;

    public static RemittanceFee of(
            String country,
            String currency,
            BigDecimal baseFee,
            BigDecimal variableFee
    ){
        return RemittanceFee.builder()
                .country(country)
                .currency(currency)
                .baseFee(baseFee)
                .variableFee(variableFee)
                .build();
    }
}
