package org.creditto.core_banking.domain.regularRemittance.entity;

import jakarta.persistence.Entity;
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
public class MonthlyRegularRemittance extends RegularRemittance{
    private Integer scheduledDate; // MONTHLY일 때만 사용 (1~31)

    public static MonthlyRegularRemittance of(
            Account account,
            Recipient recipient,
            String sendCurrency,
            String receivedCurrency,
            BigDecimal sendAmount,
            Integer scheduledDate
    ) {
        return MonthlyRegularRemittance.builder()
                .account(account)
                .recipient(recipient)
                .sendCurrency(sendCurrency)
                .receivedCurrency(receivedCurrency)
                .sendAmount(sendAmount)
                .regRemStatus(RegRemStatus.ACTIVE)
                .scheduledDate(scheduledDate)
                .build();
    }
}
