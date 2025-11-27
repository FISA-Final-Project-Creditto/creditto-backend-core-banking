package org.creditto.core_banking.domain.regularremittance.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.global.common.CurrencyCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "regrem_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public abstract class RegularRemittance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long regRemId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    private Recipient recipient;

    @Enumerated(EnumType.STRING)
    private CurrencyCode sendCurrency;

    @Enumerated(EnumType.STRING)
    private CurrencyCode receivedCurrency;

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

    public void updateRegRemStatus(RegRemStatus regRemStatus) {
        this.regRemStatus = regRemStatus;
    }

    public void updateDetails(
            Account account,
            BigDecimal sendAmount,
            RegRemStatus regRemStatus
    ) {
        this.account = account;
        this.sendAmount = sendAmount;
        this.regRemStatus = regRemStatus;
    }
}
