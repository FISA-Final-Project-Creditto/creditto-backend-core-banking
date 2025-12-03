package org.creditto.core_banking.domain.exchange.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.global.common.BaseEntity;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Exchange extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CurrencyCode fromCurrency;

    @Enumerated(EnumType.STRING)
    private CurrencyCode toCurrency;

    @Column(precision = 20, scale = 2)
    private BigDecimal fromAmount;

    @Column(precision = 20, scale = 2)
    private BigDecimal toAmount;

    @Column(precision = 20, scale = 6)
    private BigDecimal exchangeRate; // 제공 환율

    public static Exchange of(ExchangeReq req, BigDecimal fromAmount, BigDecimal toAmount, BigDecimal exchangeRate) {
        return Exchange.builder()
                .fromCurrency(req.fromCurrency())
                .toCurrency(req.toCurrency())
                .fromAmount(fromAmount)
                .toAmount(toAmount)
                .exchangeRate(exchangeRate)
                .build();
    }
}
