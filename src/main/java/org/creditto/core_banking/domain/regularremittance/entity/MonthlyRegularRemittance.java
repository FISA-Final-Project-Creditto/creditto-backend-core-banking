package org.creditto.core_banking.domain.regularremittance.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@DiscriminatorValue("MONTHLY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyRegularRemittance extends RegularRemittance{
    private Integer scheduledDate; // MONTHLY일 때만 사용 (1~31)

    public void updateSchedule(Integer scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public static MonthlyRegularRemittance of(
            Account account,
            Recipient recipient,
            CurrencyCode sendCurrency,
            CurrencyCode receivedCurrency,
            BigDecimal sendAmount,
            Integer scheduledDate,
            LocalDate startedAt
    ) {
        return MonthlyRegularRemittance.builder()
                .account(account)
                .recipient(recipient)
                .sendCurrency(sendCurrency)
                .receivedCurrency(receivedCurrency)
                .sendAmount(sendAmount)
                .regRemStatus(RegRemStatus.ACTIVE)
                .scheduledDate(scheduledDate)
                .startedAt(startedAt)
                .build();
    }
}
