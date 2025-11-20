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

import java.math.BigDecimal;

@Entity
@Getter
@SuperBuilder
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "regrem_type")
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

    @Enumerated(EnumType.STRING)
    private CurrencyCode sendCurrency;

    @Enumerated(EnumType.STRING)
    private CurrencyCode receivedCurrency;

    private BigDecimal sendAmount;

    @Enumerated(EnumType.STRING)
    private RegRemStatus regRemStatus;

    public void updateDetails(
            Account account,
            Recipient recipient,
            CurrencyCode sendCurrency,
            CurrencyCode receivedCurrency,
            BigDecimal sendAmount,
            RegRemStatus regRemStatus
    ) {
        this.account = account;
        this.recipient = recipient;
        this.sendCurrency = sendCurrency;
        this.receivedCurrency = receivedCurrency;
        this.sendAmount = sendAmount;
        this.regRemStatus = regRemStatus;
    }
}
