package org.creditto.core_banking.domain.exchange.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.account.entity.Account;
import org.creditto.core_banking.domain.account.repository.AccountRepository;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRateRes;
import org.creditto.core_banking.domain.exchange.dto.ExchangeReq;
import org.creditto.core_banking.domain.exchange.dto.ExchangeRes;
import org.creditto.core_banking.domain.exchange.entity.Exchange;
import org.creditto.core_banking.domain.exchange.repository.ExchangeRepository;
import org.creditto.core_banking.global.feign.ExchangeRateProvider;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {

    private final ExchangeRateProvider exchangeRateProvider;
    private final AccountRepository accountRepository;
    private final ExchangeRepository exchangeRepository;


    @Override
    @Transactional
    public ExchangeRes exchange(ExchangeReq request) {

        List<ExchangeRateRes> rates = exchangeRateProvider.getExchangeRates();
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(ErrorBaseCode.NOT_FOUND_ENTITY.getMessage()));

        BigDecimal finalExchangeAmount; // 최종적으로 받은 금액
        BigDecimal appliedRate; // 실제 적용된 환율

        // 원화 -> 외화 환전
        if ("KRW".equalsIgnoreCase(request.getFromCurrency())) {
            ExchangeRateRes rateDto = findRate(rates, request.getToCurrency());
            BigDecimal sellRate = new BigDecimal(rateDto.getSellRate().replace(",", "")); // 서비스가 외화를 파는 환율
            appliedRate = sellRate;

            BigDecimal krwDebitAmount = request.getTargetAmount().multiply(sellRate);

            if (account.getBalance().compareTo(krwDebitAmount) < 0) {
                throw new RuntimeException("계좌 잔액이 부족합니다.");
            }
            account.updateBalance(krwDebitAmount.negate()); // 계산된 원화 금액 차감

            saveExchangeHistory(account, request.getFromCurrency(), request.getToCurrency(), krwDebitAmount, request.getTargetAmount(), sellRate);
            finalExchangeAmount = request.getTargetAmount();

        // 외화 -> 원화 환전
        } else if ("KRW".equalsIgnoreCase(request.getToCurrency())) {
            ExchangeRateRes rateDto = findRate(rates, request.getFromCurrency());
            BigDecimal buyRate = new BigDecimal(rateDto.getBuyRate().replace(",", "")); // 서비스가 외화를 사는 환율
            appliedRate = buyRate;

            BigDecimal foreignCurrencyAmount = request.getTargetAmount().divide(buyRate, 2, java.math.RoundingMode.HALF_UP);
            account.updateBalance(request.getTargetAmount()); // targetAmount(원화)만큼 계좌에 입금

            saveExchangeHistory(account, request.getFromCurrency(), request.getToCurrency(), foreignCurrencyAmount, request.getTargetAmount(), buyRate);
            finalExchangeAmount = request.getTargetAmount();

        } else {
            throw new IllegalArgumentException("환전은 원화(KRW)가 포함되어야 합니다.");
        }

        accountRepository.save(account);

        return ExchangeRes.builder()
                .fromCurrency(request.getFromCurrency())
                .toCurrency(request.getToCurrency())
                .rate(appliedRate)
                .exchangeAmount(finalExchangeAmount)
                .build();
    }


    private ExchangeRateRes findRate(List<ExchangeRateRes> rates, String currency) {
        return rates.stream()
                .filter(r -> r.getCurrencyUnit().equalsIgnoreCase(currency))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 통화(" + currency + ")의 환율 정보를 찾을 수 없습니다."));
    }

    private void saveExchangeHistory(Account account, String fromCurrency, String toCurrency, BigDecimal fromAmount, BigDecimal toAmount, BigDecimal rate) {
        Exchange exchange = Exchange.of(
                account,
                fromCurrency,
                toCurrency,
                fromAmount,
                toAmount,
                rate
        );
        exchangeRepository.save(exchange);
    }
}
