package org.creditto.core_banking.domain.exchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// 외부 API 응답 매핑 DTO
public class ExchangeRateRes {

    private Integer result; // 응답 결과

    @JsonProperty("cur_unit")
    private String currencyUnit; // 통화 코드

    @JsonProperty("deal_bas_r")
    private String baseRate; // 매매 기준율

    @JsonProperty("cur_nm")
    private String currencyName; // 통화명

}
