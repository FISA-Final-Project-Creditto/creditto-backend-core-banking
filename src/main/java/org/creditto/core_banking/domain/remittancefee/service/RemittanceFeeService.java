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

        BigDecimal sendAmountInUSD = getSendAmountInUSD(sendAmount, currency);

        // 수수료 계산에 사용될 각 정책 엔티티 조회
        FlatServiceFee flatFeePolicy = getFlatFeePolicy(sendAmountInUSD);
        PctServiceFee pctFeePolicy = getPctFeePolicy();
        NetworkFee networkFeePolicy = getNetworkFeePolicy(currency);

        // 각 수수료를 원화 기준으로 계산
//        BigDecimal flatFeeInKRW = calculateFlatFee(flatFeePolicy, sendAmount, exchangeRate);
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

    private BigDecimal getSendAmountInUSD(BigDecimal sendAmount, String currency) {
        // TODO: JPY -> USD / CNY -> USD를 계산하기 위한 KRW -> USD 필요. 추후 교체 예정
        if (currency.equals("JPY")) return sendAmount.multiply(BigDecimal.valueOf(0.0065));
        else if (currency.equals("CNY")) return sendAmount.multiply(BigDecimal.valueOf(0.13));

        return sendAmount;
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

//    private BigDecimal calculateFlatFee(FlatServiceFee policy, BigDecimal sendAmount, BigDecimal exchangeRate) {
//        BigDecimal feeAmountInForeignCurrency = flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmount)
//                .map(FlatServiceFee::getFeeAmount)
//                .orElseThrow(() -> new CustomException("Flat service fee tier not found for amount: " + sendAmount));
//
//        // RoundingMode.HALF_UP 할지 DOWN 할지 고민 - 논의 필요
//        return feeAmountInForeignCurrency.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
//
//    }

    private BigDecimal calculatePctFee(PctServiceFee policy, BigDecimal sendAmount, BigDecimal exchangeRate, String currency) {
        // isActive 상태에 따라 분기 처리
        if (policy != null && policy.getIsActive()) {
            BigDecimal feeRate = policy.getFeeRate().divide(BigDecimal.valueOf(100));
            BigDecimal calculatedSendAmount = sendAmount;
            if (currency.equals("JPY")) {
                calculatedSendAmount = calculatedSendAmount.divide(BigDecimal.valueOf(100));
            }
            return calculatedSendAmount.multiply(feeRate).multiply(exchangeRate).setScale(0, RoundingMode.DOWN);
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
        return calculatedSendAmount.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
    }
}
