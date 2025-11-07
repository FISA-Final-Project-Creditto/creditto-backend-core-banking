package org.creditto.core_banking.domain.exchange.entity;

import jakarta.persistence.*;
import lombok.*;
import org.creditto.core_banking.domain.account.entity.Account;
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

    private String fromCurrency;

    private String toCurrency;

    @Column(precision = 20, scale = 2)
    private BigDecimal fromAmount;

    @Column(precision = 20, scale = 2)
    private BigDecimal toAmount;

    @Column(precision = 20, scale = 6) // 환율은 더 높은 정밀도가 필요할 수 있습니다.
    private BigDecimal exchangeRate;

    public static Exchange of(Account account, String fromCurrency, String toCurrency, BigDecimal fromAmount, BigDecimal toAmount, BigDecimal exchangeRate) {
        return Exchange.builder()
                .account(account)
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .fromAmount(fromAmount)
                .toAmount(toAmount)
                .exchangeRate(exchangeRate)
                .build();
    }
}
