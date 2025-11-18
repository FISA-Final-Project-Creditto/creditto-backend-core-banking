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
public class PctServiceFee extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pctServiceFeeId;

    private BigDecimal feeRate;

    private Boolean isActive;

    public static PctServiceFee of(
            Long pctServiceFeeId,
            BigDecimal feeRate,
            Boolean isActive
    ) {
        return PctServiceFee.builder()
                .pctServiceFeeId(pctServiceFeeId)
                .feeRate(feeRate)
                .isActive(isActive)
                .build();
    }
}