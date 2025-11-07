package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.creditto.core_banking.domain.regularremittance.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegularRemittanceResponseDto {
    private Long regRemId;
    private Long accountId;
    private Long recipientId;
    private String sendCurrency;
    private String receivedCurrency;
    private BigDecimal sendAmount;
    private RegRemStatus regRemStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String regremType; // "MONTHLY" 또는 "WEEKLY"
    private Integer scheduledDate; // MONTHLY일 경우 (1~31)
    private ScheduledDay scheduledDay; // WEEKLY일 경우

    public static RegularRemittanceResponseDto from(RegularRemittance regularRemittance) {
        RegularRemittanceResponseDtoBuilder builder = RegularRemittanceResponseDto.builder()
                .regRemId(regularRemittance.getRegRemId())
                .accountId(regularRemittance.getAccount().getId())
                .recipientId(regularRemittance.getRecipient().getRecipientId())
                .sendCurrency(regularRemittance.getSendCurrency())
                .receivedCurrency(regularRemittance.getReceivedCurrency())
                .sendAmount(regularRemittance.getSendAmount())
                .regRemStatus(regularRemittance.getRegRemStatus())
                .createdAt(regularRemittance.getCreatedAt())
                .updatedAt(regularRemittance.getUpdatedAt());

        if (regularRemittance instanceof MonthlyRegularRemittance monthly) {
            builder.regremType("MONTHLY")
                   .scheduledDate(monthly.getScheduledDate());
        } else if (regularRemittance instanceof WeeklyRegularRemittance weekly) {
            builder.regremType("WEEKLY")
                   .scheduledDay(weekly.getScheduledDay());
        }

        return builder.build();
    }
}
