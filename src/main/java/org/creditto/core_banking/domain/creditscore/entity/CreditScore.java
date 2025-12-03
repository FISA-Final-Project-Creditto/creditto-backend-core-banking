package org.creditto.core_banking.domain.creditscore.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.global.common.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CreditScore extends BaseEntity {

    @Id
    private Long userId;

    @Column(name = "score", nullable = false)
    private Integer score;
}
