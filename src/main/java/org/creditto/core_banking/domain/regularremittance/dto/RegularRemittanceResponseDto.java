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

    private String recipientName;
    private String recipientBankName;

    private CurrencyCode sendCurrency;
    private BigDecimal sendAmount;
    private CurrencyCode receivedCurrency;
    private RegRemStatus regRemStatus;

    private String regRemType;            // 매월/매주
    private Integer scheduledDate;        // 매월 송금
    private ScheduledDay scheduledDay;    // 매주 송금

    public static RegularRemittanceResponseDto from(RegularRemittance regularRemittance) {
        RegularRemittanceResponseDto.RegularRemittanceResponseDtoBuilder builder = RegularRemittanceResponseDto.builder()
                .regRemId(regularRemittance.getRegRemId())
                .recipientName(regularRemittance.getRecipient().getName())
                .recipientBankName(regularRemittance.getRecipient().getBankName())
//                .accountId(regularRemittance.getAccount().getId())
//                .recipientId(regularRemittance.getRecipient().getRecipientId())
                .sendCurrency(regularRemittance.getSendCurrency())
                .sendAmount(regularRemittance.getSendAmount())
                .receivedCurrency(regularRemittance.getReceivedCurrency())
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
