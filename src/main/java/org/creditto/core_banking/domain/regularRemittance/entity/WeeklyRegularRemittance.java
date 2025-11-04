package org.creditto.core_banking.domain.regularRemittance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.recipient.entity.Recipient;

import java.math.BigDecimal;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyRegularRemittance extends RegularRemittance{

    @Enumerated(EnumType.STRING)
    private ScheduledDay scheduledDay;

    public static WeeklyRegularRemittance of(
            Account account,
            Recipient recipient,
            String sendCurrency,
            String receivedCurrency,
            BigDecimal sendAmount,
            ScheduledDay scheduledDay
    ) {
        return WeeklyRegularRemittance.builder()
                .account(account)
                .recipient(recipient)
                .sendCurrency(sendCurrency)
                .receivedCurrency(receivedCurrency)
                .sendAmount(sendAmount)
                .regRemStatus(RegRemStatus.ACTIVE)
                .scheduledDay(scheduledDay)
                .build();
    }

}
