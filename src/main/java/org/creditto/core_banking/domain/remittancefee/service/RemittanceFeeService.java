package org.creditto.core_banking.domain.remittancefee.service;

import org.creditto.core_banking.domain.remittancefee.dto.RemittanceFeeReq;
import org.creditto.core_banking.domain.remittancefee.entity.FlatServiceFee;
import org.creditto.core_banking.domain.remittancefee.entity.NetworkFee;
import org.creditto.core_banking.domain.remittancefee.entity.PctServiceFee;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RemittanceFeeService {

    private final FlatServiceFeeRepository flatServiceFeeRepository;
    private final PctServiceFeeRepository pctServiceFeeRepository;
    private final NetworkFeeRepository networkFeeRepository;

    public BigDecimal calculateTotalFeeInKRW(RemittanceFeeReq req) {
        BigDecimal sendAmount = req.sendAmount();
        BigDecimal exchangeRate = req.exchangeRate();
        String currency = req.currency();

        // 각 수수료를 원화 기준으로 계산
        BigDecimal flatFeeInKRW = calculateFlatFee(sendAmount, exchangeRate);
        BigDecimal pctFeeInKRW = calculatePctFee(sendAmount, exchangeRate); // 현재 비활성화(isActive = false)
        BigDecimal networkFeeInKRW = calculateNetworkFee(currency, exchangeRate);

        // 모든 수수료를 합산 (원화 기준)
        return flatFeeInKRW.add(pctFeeInKRW).add(networkFeeInKRW);
    }

    private BigDecimal calculateFlatFee(BigDecimal sendAmount, BigDecimal exchangeRate) {
        BigDecimal feeAmountInForeignCurrency = flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmount)
            .map(FlatServiceFee::getFeeAmount)
            .orElseThrow(() -> new CustomException("Flat service fee tier not found for amount: " + sendAmount));

        // TODO: 송금 금액, 통화 코드로 구간 판별 후 고정 수수료 저장하는 로직으로 변경

        // RoundingMode.HALF_UP 할지 DOWN 할지 고민 - 논의 필요
        return feeAmountInForeignCurrency.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculatePctFee(BigDecimal sendAmount, BigDecimal exchangeRate) {
        PctServiceFee activeFee = pctServiceFeeRepository.findFirstByIsActiveTrue()
            .orElseThrow(() -> new CustomException("No active percentage service fee found."));

        BigDecimal feeRate = activeFee.getFeeRate();
        return sendAmount.multiply(feeRate).multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateNetworkFee(String currency, BigDecimal exchangeRate) {
        BigDecimal feeAmountInForeignCurrency = networkFeeRepository.findByCurrencyCode(currency)
            .map(NetworkFee::getFeeAmount)
            .orElseThrow(() -> new CustomException("Network fee not found for currency: " + currency));

        // TODO: 목표 송금 통화코드 비교 후 네트워크 수수료 저장하는 로직으로 변경

        return feeAmountInForeignCurrency.multiply(exchangeRate).setScale(0, RoundingMode.HALF_UP);
    }
}
