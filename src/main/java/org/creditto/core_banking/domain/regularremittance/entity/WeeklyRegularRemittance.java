package org.creditto.core_banking.domain.regularremittance.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.recipient.entity.Recipient;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder
@DiscriminatorValue("WEEKLY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyRegularRemittance extends RegularRemittance{

    private DayOfWeek scheduledDay;

    public void updateSchedule(DayOfWeek scheduledDay) {
        this.scheduledDay = scheduledDay;
    }

    public static WeeklyRegularRemittance of(
            Account account,
            Recipient recipient,
            CurrencyCode sendCurrency,
            CurrencyCode receivedCurrency,
            BigDecimal sendAmount,
            DayOfWeek scheduledDay,
            LocalDate startedAt
    ) {
        return WeeklyRegularRemittance.builder()
                .account(account)
                .recipient(recipient)
                .sendCurrency(sendCurrency)
                .receivedCurrency(receivedCurrency)
                .sendAmount(sendAmount)
                .regRemStatus(RegRemStatus.ACTIVE)
                .scheduledDay(scheduledDay)
                .startedAt(startedAt)
                .build();
    }

}
