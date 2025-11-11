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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRateProvider exchangeRateProvider;
    private final AccountRepository accountRepository;
    private final ExchangeRepository exchangeRepository;

    // 환전 시 적용될 스프레드 비율 (1%)
    private static final BigDecimal SPREAD_RATE = new BigDecimal("0.01");

    // 환전 시 적용될 환율 우대 비율 (50%)
    // TODO: 추후 사용자별 우대 환율 업데이트
    private static final BigDecimal PREFERENTIAL_RATE = new BigDecimal("0.5");

    // 원화 통화 코드 상수
    public static final String KRW_CURRENCY_CODE = "KRW";

    /**
     * 외부 API를 통해 최신 환율 정보를 조회합니다.
     *
     * @return 최신 환율 정보 리스트
     */
    public List<ExchangeRateRes> getLatestRates() {
        return exchangeRateProvider.getExchangeRates();
    }

    /**
     * 환전 요청을 처리하는 메인 메서드
     * 환전 방향에 따라 적절한 private 메서드를 호출하여 처리를 위임
     *
     * @param request 환전 요청 정보 (계좌 ID, 출발/도착 통화, 대상 금액 등)
     * @return 환전 결과 정보 (출발/도착 통화, 적용 환율, 환전 금액)
     */
    @Transactional
    public ExchangeRes exchange(ExchangeReq request) {
        // 최신 환율 정보 조회
        List<ExchangeRateRes> rates = exchangeRateProvider.getExchangeRates();
        // 요청 계좌 정보 조회, 없으면 예외 발생
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌를 찾을 수 없습니다."));

        // 원화 -> 외화 환전 로직
        if (KRW_CURRENCY_CODE.equalsIgnoreCase(request.fromCurrency())) {
            return handleKrwToForeignExchange(request, account, rates);
        }
        // 외화 -> 원화 환전 로직
        else if (KRW_CURRENCY_CODE.equalsIgnoreCase(request.toCurrency())) {
            return handleForeignToKrwExchange(request, account, rates);
        }
        // 지원하지 않는 거래 예외 처리
        else {
            throw new IllegalArgumentException("현재 지원하지 않는 서비스입니다.");
        }
    }

    /**
     * 원화에서 외화로의 환전 처리
     */
    private ExchangeRes handleKrwToForeignExchange(ExchangeReq request, Account account, List<ExchangeRateRes> rates) {
        // 대상 외화의 매매 기준율 정보 조회
        ExchangeRateRes rateInfo = findRate(rates, request.toCurrency());
        BigDecimal baseRate = new BigDecimal(rateInfo.getBaseRate().replace(",", ""));

        // 살 때 환율 계산: 매매기준율 * (1 + (스프레드 * (1 - 우대율)))
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.add(effectiveSpread));

        // 출금될 원화 금액 계산
        BigDecimal toAmount = request.targetAmount();
        BigDecimal fromAmount = toAmount.multiply(appliedRate).setScale(0, RoundingMode.CEILING);

        account.withdraw(fromAmount);

        // 환전 내역 저장 및 결과 반환
        saveExchangeHistory(account, request, fromAmount, toAmount, appliedRate);
        return new ExchangeRes(
                request.fromCurrency(),
                request.toCurrency(),
                request.country(),
                appliedRate.setScale(2, RoundingMode.HALF_UP),
                fromAmount
        );
    }

    /**
     * 외화에서 원화로의 환전 처리
     */
    private ExchangeRes handleForeignToKrwExchange(ExchangeReq request, Account account, List<ExchangeRateRes> rates) {
        // 출발 외화의 매매 기준율 정보 조회
        ExchangeRateRes rateInfo = findRate(rates, request.fromCurrency());
        BigDecimal baseRate = new BigDecimal(rateInfo.getBaseRate().replace(",", ""));

        // 팔 때 환율 계산: 매매기준율 * (1 - (스프레드 * (1 - 우대율)))
        BigDecimal effectiveSpread = SPREAD_RATE.multiply(BigDecimal.ONE.subtract(PREFERENTIAL_RATE));
        BigDecimal appliedRate = baseRate.multiply(BigDecimal.ONE.subtract(effectiveSpread));

        // 외화 기준 금액
        BigDecimal fromAmount = request.targetAmount();
        // 입금될 원화 금액 계산
        BigDecimal krwReceived = fromAmount.multiply(appliedRate).setScale(0, RoundingMode.FLOOR);

        // 원화 계좌에 입금
        account.deposit(krwReceived);

        // 환전 내역 저장 및 결과 반환
        saveExchangeHistory(account, request, fromAmount, krwReceived, appliedRate);
        return new ExchangeRes(
                request.fromCurrency(),
                request.toCurrency(),
                request.country(),
                appliedRate.setScale(2, RoundingMode.HALF_UP),
                fromAmount
        );
    }

    /**
     * 전체 환율 리스트에서 특정 통화에 해당하는 환율 정보를 찾는 메서드
     */
    private ExchangeRateRes findRate(List<ExchangeRateRes> rates, String currency) {
        return rates.stream()
                .filter(r -> r.getCurrencyUnit().equalsIgnoreCase(currency))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 통화(" + currency + ")의 환율 정보를 찾을 수 없습니다."));
    }

    /**
     * 환전 내역을 데이터베이스에 저장
     */
    private void saveExchangeHistory(Account account, ExchangeReq exchangeReq, BigDecimal fromAmount, BigDecimal toAmount, BigDecimal rate) {
        Exchange exchange = Exchange.of(
                account,
                exchangeReq,
                fromAmount,
                toAmount,
                rate
        );
        exchangeRepository.save(exchange);
    }
}
