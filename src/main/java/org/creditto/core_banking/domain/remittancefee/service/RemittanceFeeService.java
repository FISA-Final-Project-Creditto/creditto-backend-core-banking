package org.creditto.core_banking.domain.remittancefee.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.creditto.core_banking.domain.remittancefee.dto.RemittanceFeeReq;
import org.creditto.core_banking.domain.remittancefee.entity.FeeRecord;
import org.creditto.core_banking.domain.remittancefee.entity.FlatServiceFee;
import org.creditto.core_banking.domain.remittancefee.entity.NetworkFee;
import org.creditto.core_banking.domain.remittancefee.entity.PctServiceFee;
import org.creditto.core_banking.domain.remittancefee.repository.FeeRecordRepository;
import org.creditto.core_banking.domain.remittancefee.repository.FlatServiceFeeRepository;
import org.creditto.core_banking.domain.remittancefee.repository.NetworkFeeRepository;
import org.creditto.core_banking.domain.remittancefee.repository.PctServiceFeeRepository;
import org.creditto.core_banking.global.common.CurrencyCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.creditto.core_banking.global.response.error.ErrorMessage.FEE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RemittanceFeeService {

    private final FlatServiceFeeRepository flatServiceFeeRepository;
    private final PctServiceFeeRepository pctServiceFeeRepository;
    private final NetworkFeeRepository networkFeeRepository;
    private final FeeRecordRepository feeRecordRepository;

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int CALCULATION_SCALE = 10;

    @Transactional
    public FeeRecord calculateAndSaveFee(RemittanceFeeReq req) {
        BigDecimal sendAmount = req.sendAmount(); // 송금 금액 (KRW)
        BigDecimal exchangeRate = req.exchangeRate(); // 제공환율 (currency code)
        CurrencyCode currency = req.currency(); // 환율 통화

        BigDecimal usdExchangeRate = req.fromAmountInUSD();
        BigDecimal amountInKRWForFlat = normalizeAmountByCurrency(sendAmount, currency)
                .multiply(exchangeRate);
        BigDecimal sendAmountForUSD = amountInKRWForFlat.divide(usdExchangeRate, CALCULATION_SCALE, RoundingMode.HALF_UP);

        // 수수료 계산에 사용될 각 정책 엔티티 조회
        FlatServiceFee flatFeePolicy = getFlatFeePolicy(sendAmountForUSD);
        PctServiceFee pctFeePolicy = getPctFeePolicy();
        NetworkFee networkFeePolicy = getNetworkFeePolicy(currency);

        // 각 수수료를 원화 기준으로 계산
        BigDecimal flatFeeInKRW = flatFeePolicy.getFeeAmount();
        BigDecimal pctFeeInKRW = calculatePctFee(pctFeePolicy, sendAmount, exchangeRate, currency); // 현재 비활성화(isActive = false)
        BigDecimal networkFeeInKRW = calculateNetworkFee(networkFeePolicy, exchangeRate);

        // 총 수수료 합산
        BigDecimal totalFeeInKRW = flatFeeInKRW.add(pctFeeInKRW).add(networkFeeInKRW);

        // FeeRecord 생성
        FeeRecord feeRecord = FeeRecord.create(totalFeeInKRW, flatFeePolicy, pctFeePolicy, networkFeePolicy);

        // FeeRecord 저장 후 반환
        return feeRecordRepository.save(feeRecord);
    }

    /**
     * 고정 수수료 조회
     * @param sendAmountInUSD 송금금액을 달러로 변환한 금액
     * @return FlatServiceFee
     */
    private FlatServiceFee getFlatFeePolicy(BigDecimal sendAmountInUSD) {
        return flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmountInUSD)
                .orElseThrow(() -> new EntityNotFoundException(FEE_NOT_FOUND));
    }

    /**
     * 비율 수수료 조회
     * @return PctServiceFee
     */
    private PctServiceFee getPctFeePolicy() {
        return pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()
                .orElseThrow(() -> new EntityNotFoundException(FEE_NOT_FOUND));
    }

    /**
     * 네트워크 수수료 조회
     * @param currency 환전하려는 통화
     * @return NetworkFee
     */
    private NetworkFee getNetworkFeePolicy(CurrencyCode currency) {
        return networkFeeRepository.findByCurrencyCode(currency)
                .orElseThrow(() -> new EntityNotFoundException(FEE_NOT_FOUND));
    }

    private BigDecimal calculatePctFee(PctServiceFee policy, BigDecimal sendAmount, BigDecimal exchangeRate, CurrencyCode currency) {
        // isActive 상태에 따라 분기 처리
        if (policy != null && policy.getIsActive()) {
            BigDecimal feeRate = policy.getFeeRate().divide(ONE_HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP);
            BigDecimal calculatedSendAmount = normalizeAmountByCurrency(sendAmount, currency);

            // TODO : RoundingMode.HALF_UP 할지 DOWN 할지 논의 필요
            return calculatedSendAmount.multiply(feeRate).multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateNetworkFee(NetworkFee policy, BigDecimal exchangeRate) {
        BigDecimal feeAmount = policy.getFeeAmount();
        BigDecimal calculatedSendAmount = normalizeAmountByCurrency(feeAmount, policy.getCurrencyCode());

        // TODO : RoundingMode.HALF_UP 할지 DOWN 할지 논의 필요
        return calculatedSendAmount.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizeAmountByCurrency(BigDecimal amount, CurrencyCode currency) {
        return  currency.equals(CurrencyCode.JPY) || currency.equals(CurrencyCode.IDR)
                ? amount.divide(ONE_HUNDRED, CALCULATION_SCALE, RoundingMode.HALF_UP)
                : amount;
    }
}
