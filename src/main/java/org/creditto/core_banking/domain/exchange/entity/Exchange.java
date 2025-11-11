package org.creditto.core_banking.domain.exchange.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.global.common.BaseEntity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private String fromCurrency; // TODO: 통화코드 Enum으로 변경 예정

    private String toCurrency; // TODO: 통화코드 Enum으로 변경 예정

    private String country; // 수취 국가

    @Column(precision = 20, scale = 2)
    private BigDecimal fromAmount;

    @Column(precision = 20, scale = 2)
    private BigDecimal toAmount;

    @Column(precision = 20, scale = 6)
    private BigDecimal exchangeRate; // 제공 환율



    public static Exchange of(Account account, ExchangeReq req, BigDecimal fromAmount, BigDecimal toAmount, BigDecimal exchangeRate) {
        return Exchange.builder()
                .account(account)
                .fromCurrency(req.fromCurrency())
                .toCurrency(req.toCurrency())
                .fromAmount(fromAmount)
                .toAmount(toAmount)
                .exchangeRate(exchangeRate)
                .build();
    }
}
