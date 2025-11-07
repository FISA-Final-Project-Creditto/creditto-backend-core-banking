package org.creditto.core_banking.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
// 내부 환전 결과 반환 DTO
public class ExchangeRes {

    private String fromCurrency;
    private String toCurrency;
    private BigDecimal rate;
    private BigDecimal exchangeAmount;

}
