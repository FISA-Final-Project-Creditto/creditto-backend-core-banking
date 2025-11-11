package org.creditto.core_banking.global.feign;

import org.creditto.core_banking.global.config.FeignConfig;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "exchange-rate",
        url = "https://www.koreaexim.go.kr",
        configuration = FeignConfig.class
)
public interface ExchangeRateFeign {

    @GetMapping("site/program/financial/exchangeJSON")
    List<ExchangeRateRes> getExchangeRate(
            @RequestParam("authkey") String authKey,
            @RequestParam("searchdate") String searchdate,
            @RequestParam("data") String data
    );
}
