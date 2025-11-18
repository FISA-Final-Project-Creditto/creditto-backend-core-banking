package org.creditto.core_banking.domain.remittancefee.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;

import java.math.BigDecimal;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FlatServiceFee extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flatServiceFeeId;

    private BigDecimal upperLimit;

    private BigDecimal feeAmount;

    public static FlatServiceFee of(
            Long flatServiceFeeId,
            BigDecimal upperLimit,
            BigDecimal feeAmount
    ) {
        return FlatServiceFee.builder()
                .flatServiceFeeId(flatServiceFeeId)
                .upperLimit(upperLimit)
                .feeAmount(feeAmount)
                .build();
    }
}
