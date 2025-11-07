package org.creditto.core_banking.domain.exchange.service;

import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;


public interface ExchangeService {

    ExchangeRes exchange(ExchangeReq exchangeReq);

}
