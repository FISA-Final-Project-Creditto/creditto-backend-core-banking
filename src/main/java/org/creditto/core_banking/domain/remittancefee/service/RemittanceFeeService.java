package org.creditto.core_banking.domain.remittancefee.service;

import org.creditto.core_banking.domain.remittancefee.dto.RemittanceFeeReq;
import org.creditto.core_banking.domain.remittancefee.entity.FeeRecord;
import org.creditto.core_banking.domain.remittancefee.entity.FlatServiceFee;
import org.creditto.core_banking.domain.remittancefee.entity.NetworkFee;
import org.creditto.core_banking.domain.remittancefee.entity.PctServiceFee;
import org.creditto.core_banking.domain.remittancefee.repository.FeeRecordRepository;
import org.creditto.core_banking.domain.remittancefee.repository.FlatServiceFeeRepository;
import org.creditto.core_banking.domain.remittancefee.repository.NetworkFeeRepository;
import org.creditto.core_banking.domain.remittancefee.repository.PctServiceFeeRepository;
import org.creditto.core_banking.global.response.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
@RequiredArgsConstructor
public class RemittanceFeeService {

    private final FlatServiceFeeRepository flatServiceFeeRepository;
    private final PctServiceFeeRepository pctServiceFeeRepository;
    private final NetworkFeeRepository networkFeeRepository;
    private final FeeRecordRepository feeRecordRepository;

    public FeeRecord calculateAndSaveFee(RemittanceFeeReq req) {
        BigDecimal sendAmount = req.sendAmount();
        BigDecimal exchangeRate = req.exchangeRate();
        String currency = req.currency();
        BigDecimal exchangeRateUSD = req.exchangeRateUSD();

        BigDecimal sendAmountInUSD = getSendAmountInUSD(sendAmount, exchangeRate, currency, exchangeRateUSD);

        // 수수료 계산에 사용될 각 정책 엔티티 조회
        FlatServiceFee flatFeePolicy = getFlatFeePolicy(sendAmountInUSD);
        PctServiceFee pctFeePolicy = getPctFeePolicy();
        NetworkFee networkFeePolicy = getNetworkFeePolicy(currency);

        // 각 수수료를 원화 기준으로 계산
        BigDecimal flatFeeInKRW = flatFeePolicy.getFeeAmount();
        BigDecimal pctFeeInKRW = calculatePctFee(pctFeePolicy, sendAmount, exchangeRate, currency); // 현재 비활성화(isActive = false)
        BigDecimal networkFeeInKRW = calculateNetworkFee(networkFeePolicy, exchangeRate);

        // 총 수수료 합산
        BigDecimal totalFeeInKRW = flatFeeInKRW.add(pctFeeInKRW).add(networkFeeInKRW);

        // FeeRecord 생성
        FeeRecord feeRecord = FeeRecord.builder()
                .totalFee(totalFeeInKRW)
                .appliedFlatServiceFee(flatFeePolicy)
                .appliedPctServiceFee(pctFeePolicy)
                .appliedNetworkFee(networkFeePolicy)
                .build();

        // FeeRecord 저장 후 반환
        return feeRecordRepository.save(feeRecord);
    }

    private BigDecimal getSendAmountInUSD(BigDecimal sendAmount, BigDecimal exchangeRate, String currency, BigDecimal exchangeRateUSD) {
        if (exchangeRateUSD == null || exchangeRateUSD.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("exchangeRateUSD cannot be null or zero.");
        }

        int calculationScale = 10;
        RoundingMode rounding = RoundingMode.HALF_UP;

        // JPY이면 100으로 나누기
        BigDecimal baseRate;
        if (currency.equals("JPY")) {
            baseRate = exchangeRate.divide(BigDecimal.valueOf(100), calculationScale, rounding);
        } else {
            baseRate = exchangeRate;
        }

        BigDecimal calculatedRate = baseRate.divide(exchangeRateUSD, calculationScale, rounding);

        BigDecimal sendAmountInUSD = sendAmount.multiply(calculatedRate);

        return sendAmountInUSD.setScale(2, RoundingMode.HALF_UP);
    }

    private FlatServiceFee getFlatFeePolicy(BigDecimal sendAmountInUSD) {
        return flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmountInUSD)
                .orElseThrow(() -> new CustomException("Flat service fee tier not found for amount: " + sendAmountInUSD));
    }

    private PctServiceFee getPctFeePolicy() {
        return pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()
                .orElseThrow(() -> new CustomException("Percentage service fee policy not found."));
    }

    private NetworkFee getNetworkFeePolicy(String currency) {
        return networkFeeRepository.findByCurrencyCode(currency)
                .orElseThrow(() -> new CustomException("Network fee not found for currency: " + currency));
    }

    private BigDecimal calculatePctFee(PctServiceFee policy, BigDecimal sendAmount, BigDecimal exchangeRate, String currency) {
        // isActive 상태에 따라 분기 처리
        if (policy != null && policy.getIsActive()) {
            BigDecimal feeRate = policy.getFeeRate().divide(BigDecimal.valueOf(100));
            BigDecimal calculatedSendAmount = sendAmount;
            if (currency.equals("JPY")) {
                calculatedSendAmount = calculatedSendAmount.divide(BigDecimal.valueOf(100));
            }
            // RoundingMode.HALF_UP 할지 DOWN 할지 고민 - 논의 필요
            return calculatedSendAmount.multiply(feeRate).multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateNetworkFee(NetworkFee policy, BigDecimal exchangeRate) {
        BigDecimal feeAmount = policy.getFeeAmount();
        BigDecimal calculatedSendAmount = feeAmount;
        if (policy.getCurrencyCode().equals("JPY")) {
            calculatedSendAmount = calculatedSendAmount.divide(BigDecimal.valueOf(100));
        }
        // RoundingMode.HALF_UP 할지 DOWN 할지 고민 - 논의 필요
        return calculatedSendAmount.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
    }
}
