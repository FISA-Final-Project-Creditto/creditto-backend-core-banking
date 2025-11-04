package org.creditto.core_banking.domain.regularRemittance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.global.common.BaseEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class RegularRemittance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long regRemId;

    @ManyToOne
    private Account account;

    @OneToOne
    private Recipient recipient;

    private String sendCurrency;

    private String receivedCurrency;

    private BigDecimal sendAmount;

    @Enumerated(EnumType.STRING)
    private RegRemStatus regRemStatus;

    // 엔티티 생성 시각 자동 저장
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    // 엔티티 수정 시각 자동 저장
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime updatedAt;
}
