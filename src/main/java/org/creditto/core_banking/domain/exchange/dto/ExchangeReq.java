package org.creditto.core_banking.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExchangeReq {

    private Long accountId; // 어떤 계좌에서 환전할건지
    private String fromCurrency; // 환전 전 통화
    private String toCurrency; // 환전 후 통화
    private BigDecimal targetAmount; // 받고 싶은 외화 금액
}
