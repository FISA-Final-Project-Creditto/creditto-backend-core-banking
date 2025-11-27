package org.creditto.core_banking.domain.regularremittance.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.creditto.core_banking.domain.regularremittance.entity.RegRemStatus;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegularRemittanceUpdateDto {

    private String accountNo;
    private BigDecimal sendAmount;
    private RegRemStatus regRemStatus;
    private Integer scheduledDate;  // 매월일 경우 수정할 날짜
    private DayOfWeek scheduledDay;    // 매주일 경우 수정할 요일
}
