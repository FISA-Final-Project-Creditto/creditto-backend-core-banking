package org.creditto.core_banking.domain.remittancefee.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_record")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class FeeRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feeRecordId;

    private BigDecimal totalFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_service_fee_id")
    private FlatServiceFee appliedFlatServiceFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pct_service_fee_id")
    private PctServiceFee appliedPctServiceFee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "network_fee_id")
    private NetworkFee appliedNetworkFee;

    public static FeeRecord create(BigDecimal totalFee, FlatServiceFee flatServiceFee, PctServiceFee pctServiceFee, NetworkFee networkFee) {
        return FeeRecord.builder()
                .totalFee(totalFee)
                .appliedFlatServiceFee(flatServiceFee)
                .appliedPctServiceFee(pctServiceFee)
                .appliedNetworkFee(networkFee)
                .build();
    }
}