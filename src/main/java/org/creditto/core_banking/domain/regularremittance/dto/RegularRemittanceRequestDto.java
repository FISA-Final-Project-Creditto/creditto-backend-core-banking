package org.creditto.core_banking.domain.regularremittance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.creditto.core_banking.domain.regularremittance.entity.ScheduledDay;

import java.math.BigDecimal;

@Getter
public class RegularRemittanceRequestDto {

    @NotNull
    private Long accountId;

    @NotNull
    private Long recipientId;

    @NotNull
    private String sendCurrency;

    @NotNull
    private String receivedCurrency;

    @NotNull
    private BigDecimal sendAmount;

    @NotNull
    private String regremType; // "MONTHLY" or "WEEKLY"

    private Integer scheduledDate; // For MONTHLY (1-31)

    private ScheduledDay scheduledDay; // For WEEKLY (e.g., "MONDAY")
}
