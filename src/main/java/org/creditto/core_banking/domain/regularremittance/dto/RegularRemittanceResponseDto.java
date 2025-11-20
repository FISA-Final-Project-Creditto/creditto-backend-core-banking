package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.regularremittance.entity.*;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;

@Getter
@Builder
public class RegularRemittanceResponseDto {
    private Long regRemId;
    private Long accountId;
    private Long recipientId;
    private CurrencyCode sendCurrency;
    private CurrencyCode receivedCurrency;
    private BigDecimal sendAmount;
    private RegRemStatus regRemStatus;

    private String regRemType;            // 매월/매주

    private Integer scheduledDate;        // 매월 송금
    private ScheduledDay scheduledDay;    // 매주 송금

    public static RegularRemittanceResponseDto from(RegularRemittance regularRemittance) {
        RegularRemittanceResponseDto.RegularRemittanceResponseDtoBuilder builder = RegularRemittanceResponseDto.builder()
                .regRemId(regularRemittance.getRegRemId())
                .accountId(regularRemittance.getAccount().getId())
                .recipientId(regularRemittance.getRecipient().getRecipientId())
                .sendCurrency(regularRemittance.getSendCurrency())
                .receivedCurrency(regularRemittance.getReceivedCurrency())
                .sendAmount(regularRemittance.getSendAmount())
                .regRemStatus(regularRemittance.getRegRemStatus());

        if (regularRemittance instanceof MonthlyRegularRemittance) {
            MonthlyRegularRemittance monthly = (MonthlyRegularRemittance) regularRemittance;
            builder.regRemType("MONTHLY")
                    .scheduledDate(monthly.getScheduledDate());
        } else if (regularRemittance instanceof WeeklyRegularRemittance) {
            WeeklyRegularRemittance weekly = (WeeklyRegularRemittance) regularRemittance;
            builder.regRemType("WEEKLY")
                    .scheduledDay(weekly.getScheduledDay());
        }

        return builder.build();
    }
}
