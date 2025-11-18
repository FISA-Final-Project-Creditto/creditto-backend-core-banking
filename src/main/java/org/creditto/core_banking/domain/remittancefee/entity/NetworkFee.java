package org.creditto.core_banking.domain.remittancefee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NetworkFee extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long networkFeeId;

    // TODO: 통화 코드 Enum 타입으로 변경
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyCode;

    private BigDecimal feeAmount;

    public static NetworkFee of(
            Long networkFeeId,
            CurrencyCode currencyCode,
            BigDecimal feeAmount
    ) {
        return NetworkFee.builder()
                .networkFeeId(networkFeeId)
                .currencyCode(currencyCode)
                .feeAmount(feeAmount)
                .build();
    }
}
