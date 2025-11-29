package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.Builder;
import lombok.Getter;
import org.creditto.core_banking.domain.regularremittance.entity.*;
import org.creditto.core_banking.global.common.CurrencyCode;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Builder
public class RegularRemittanceResponseDto {
    private String accountNo;

    private Long regRemId;

    private String recipientName;
    private String recipientBankName;

    private BigDecimal sendAmount;
    private CurrencyCode receivedCurrency;
    private RegRemStatus regRemStatus;

    private String regRemType;          // 매월/매주
    private Integer scheduledDate;      // 매월 송금
    private DayOfWeek scheduledDay;     // 매주 송금

    private LocalDate startedAt;        // 송금 시작일

    public static RegularRemittanceResponseDto from(RegularRemittance regularRemittance) {
        RegularRemittanceResponseDto.RegularRemittanceResponseDtoBuilder builder = RegularRemittanceResponseDto.builder()
                .accountNo(regularRemittance.getAccount().getAccountNo())
                .regRemId(regularRemittance.getRegRemId())
                .recipientName(regularRemittance.getRecipient().getName())
                .recipientBankName(regularRemittance.getRecipient().getBankName())
                .sendAmount(regularRemittance.getSendAmount())
                .receivedCurrency(regularRemittance.getReceivedCurrency())
                .regRemStatus(regularRemittance.getRegRemStatus())
                .startedAt(regularRemittance.getStartedAt());

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
