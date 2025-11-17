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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemittanceFeeServiceTest {

    @InjectMocks
    private RemittanceFeeService remittanceFeeService;

    @Mock
    private FlatServiceFeeRepository flatServiceFeeRepository;
    @Mock
    private PctServiceFeeRepository pctServiceFeeRepository;
    @Mock
    private NetworkFeeRepository networkFeeRepository;
    @Mock
    private FeeRecordRepository feeRecordRepository;

    private List<FlatServiceFee> flatFeeTiers;
    private FlatServiceFee flatFeePolicy;
    private PctServiceFee pctFeePolicyInactive;
    private PctServiceFee pctFeePolicyActive;
    private NetworkFee networkFeePolicyUSD;
    private NetworkFee networkFeePolicyJPY;
    private NetworkFee networkFeePolicyCNY;

    @BeforeEach
    void setUp() {
        // Mock 정책 데이터 설정
        flatFeeTiers = List.of(
                FlatServiceFee.of(1L, new BigDecimal("500"), new BigDecimal("2500")),
                FlatServiceFee.of(2L, new BigDecimal("3000"), new BigDecimal("5000")),
                FlatServiceFee.of(3L, new BigDecimal("5000"), new BigDecimal("7500")),
                FlatServiceFee.of(4L, new BigDecimal("9999999999"), new BigDecimal("10000"))
        );
        flatFeePolicy = FlatServiceFee.of(1L, new BigDecimal("3000"), new BigDecimal("7500"));
        pctFeePolicyInactive = PctServiceFee.of(1L, new BigDecimal("0.2"), false);
        pctFeePolicyActive = PctServiceFee.of(1L, new BigDecimal("0.2"), true);
        networkFeePolicyUSD = NetworkFee.of(1L, "USD", new BigDecimal("15"));
        networkFeePolicyJPY = NetworkFee.of(2L, "JPY", new BigDecimal("2000"));
        networkFeePolicyCNY = NetworkFee.of(3L, "CNY", new BigDecimal("100"));
    }

    @Nested
    @DisplayName("수수료 계산 성공 케이스")
    class SuccessCases {

        @Test
        @DisplayName("USD 송금 (PctFee 비활성)")
        void calculateAndSaveFee_ForUSD_When_PctFeeInactive() {
            // given
            BigDecimal sendAmount = new BigDecimal("3000");
            BigDecimal exchangeRate = new BigDecimal("1458.86");
            String currency = "USD";
            BigDecimal exchangeRateUSD = new BigDecimal("1458.86");
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency, exchangeRateUSD);

//            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmount))
//                    .thenReturn(Optional.of(flatFeePolicy));
            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(any(BigDecimal.class)))
                    .thenAnswer(invocation -> {
                        BigDecimal amountInUSD = invocation.getArgument(0, BigDecimal.class);
                        return flatFeeTiers.stream()
                                .filter(tier -> tier.getUpperLimit().compareTo(amountInUSD) >= 0)
                                .findFirst();
                    });
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyInactive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyUSD));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 5000
            // pctFeeInKRW = 0 (비활성)
            // networkFeeInKRW = 15 * 1458.86 = 21883
            // totalFee = 5000 + 0 + 21883 = 26883
            BigDecimal expectedTotalFee = new BigDecimal("26883");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }

        @Test
        @DisplayName("JPY 송금 (PctFee 비활성)")
        void calculateAndSaveFee_ForJPY_When_PctFeeInactive() {
            // given
            BigDecimal sendAmount = new BigDecimal("77000"); // 77000엔
            BigDecimal exchangeRate = new BigDecimal("942.48"); // 100엔 = 942.48원
            String currency = "JPY";
            BigDecimal exchangeRateUSD = new BigDecimal("1458.86");
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency, exchangeRateUSD);

            BigDecimal sendAmountInUSD = sendAmount.multiply(new BigDecimal(0.6460386877)).setScale(2, RoundingMode.HALF_UP);

//            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmountInUSD))
//                    .thenReturn(Optional.of(flatFeePolicy));
            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(any(BigDecimal.class)))
                    .thenAnswer(invocation -> {
                        BigDecimal amountInUSD = invocation.getArgument(0, BigDecimal.class);
                        return flatFeeTiers.stream()
                                .filter(tier -> tier.getUpperLimit().compareTo(amountInUSD) >= 0)
                                .findFirst();
                    });
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyInactive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyJPY));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 2500
            // pctFeeInKRW = 0 (비활성)
            // networkFeeInKRW = (2000 / 100) * 942.48 = 18850
            // totalFee = 2500 + 0 + 18850 = 21350
            BigDecimal expectedTotalFee = new BigDecimal("21350");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }

        @Test
        @DisplayName("CNH 송금 (PctFee 비활성)")
        void calculateAndSaveFee_ForCNY_When_PctFeeInactive() {
            // given
            BigDecimal sendAmount = new BigDecimal("3500"); // 3500위안
            BigDecimal exchangeRate = new BigDecimal("205.35"); // 1위안 = 205.35원
            String currency = "CNH";
            BigDecimal exchangeRateUSD = new BigDecimal("1458.86");
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency, exchangeRateUSD);

            // CNY -> USD 변환: 5000 * 0.13 = 650
            BigDecimal sendAmountInUSD = sendAmount.multiply(new BigDecimal(0.1407605938883786)).setScale(2, RoundingMode.HALF_UP);

//            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmountInUSD))
//                    .thenReturn(Optional.of(flatFeePolicy));
            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(any(BigDecimal.class)))
                    .thenAnswer(invocation -> {
                        BigDecimal amountInUSD = invocation.getArgument(0, BigDecimal.class);
                        return flatFeeTiers.stream()
                                .filter(tier -> tier.getUpperLimit().compareTo(amountInUSD) >= 0)
                                .findFirst();
                    });
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyInactive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyCNY));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 2500
            // pctFeeInKRW = 0 (비활성)
            // networkFeeInKRW = 100 * 205.35 = 20535
            // totalFee = 2500 + 0 + 20535 = 23035.00
            BigDecimal expectedTotalFee = new BigDecimal("23035.00");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }

        @Test
        @DisplayName("USD 송금 (PctFee 활성)")
        void calculateAndSaveFee_ForUSD_When_PctFeeActive() {
            // given
            BigDecimal sendAmount = new BigDecimal("3000");
            BigDecimal exchangeRate = new BigDecimal("1458.86");
            String currency = "USD";
            BigDecimal exchangeRateUSD = new BigDecimal("1458.86");
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency, exchangeRateUSD);

//            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmount))
//                    .thenReturn(Optional.of(flatFeePolicy));
            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(any(BigDecimal.class)))
                    .thenAnswer(invocation -> {
                        BigDecimal amountInUSD = invocation.getArgument(0, BigDecimal.class);
                        return flatFeeTiers.stream()
                                .filter(tier -> tier.getUpperLimit().compareTo(amountInUSD) >= 0)
                                .findFirst();
                    });
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyActive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyUSD));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 5000
            // pctFeeInKRW = 3000 * (0.2 / 100) * 1458.86 = 8753
            // networkFeeInKRW = 15 * 1300 = 21883
            // totalFee = 5000 + 8753 + 21883 = 35636
            BigDecimal expectedTotalFee = new BigDecimal("35636");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }

        @Test
        @DisplayName("JPY 송금 (PctFee 활성)")
        void calculateAndSaveFee_ForJPY_When_PctFeeActive() {
            // given
            BigDecimal sendAmount = new BigDecimal("77000"); // 77000엔
            BigDecimal exchangeRate = new BigDecimal("942.48"); // 100엔 = 942.48원
            String currency = "JPY";
            BigDecimal exchangeRateUSD = new BigDecimal("1458.86");
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency, exchangeRateUSD);

            BigDecimal sendAmountInUSD = sendAmount.multiply(new BigDecimal(0.6460386877)).setScale(2, RoundingMode.HALF_UP);

//            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmountInUSD))
//                    .thenReturn(Optional.of(flatFeePolicy));
            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(any(BigDecimal.class)))
                    .thenAnswer(invocation -> {
                        BigDecimal amountInUSD = invocation.getArgument(0, BigDecimal.class);
                        return flatFeeTiers.stream()
                                .filter(tier -> tier.getUpperLimit().compareTo(amountInUSD) >= 0)
                                .findFirst();
                    });
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyActive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyJPY));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 2500
            // pctFeeInKRW = (77000 / 100) * (0.2 / 100) * 942.48 = 1451
            // networkFeeInKRW = (2000 / 100) * 942.48 = 18850
            // totalFee = 2500 + 1451 + 18850 = 22801
            BigDecimal expectedTotalFee = new BigDecimal("22801");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }
    }

    @Nested
    @DisplayName("수수료 계산 예외 케이스")
    class ExceptionCases {

        @Test
        @DisplayName("FlatServiceFee 정책을 찾을 수 없을 때 CustomException 발생")
        void calculateAndSaveFee_ThrowsException_When_FlatFeePolicyNotFound() {
            // given
            RemittanceFeeReq req = new RemittanceFeeReq(BigDecimal.ONE, BigDecimal.TEN, "USD", BigDecimal.valueOf(1300));
            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(any(BigDecimal.class)))
                    .thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () -> {
                remittanceFeeService.calculateAndSaveFee(req);
            });
            assertThat(exception.getMessage()).contains("Flat service fee tier not found for amount");
        }

        @Test
        @DisplayName("PctServiceFee 정책을 찾을 수 없을 때 CustomException 발생")
        void calculateAndSaveFee_ThrowsException_When_PctFeePolicyNotFound() {
            // given
            RemittanceFeeReq req = new RemittanceFeeReq(BigDecimal.ONE, BigDecimal.TEN, "USD", BigDecimal.valueOf(1300));
            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(any(BigDecimal.class)))
                    .thenReturn(Optional.of(flatFeePolicy));
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () -> {
                remittanceFeeService.calculateAndSaveFee(req);
            });
            assertEquals("Percentage service fee policy not found.", exception.getMessage());
        }

        @Test
        @DisplayName("NetworkFee 정책을 찾을 수 없을 때 CustomException 발생")
        void calculateAndSaveFee_ThrowsException_When_NetworkFeePolicyNotFound() {
            // given
            String currency = "XYZ";
            RemittanceFeeReq req = new RemittanceFeeReq(BigDecimal.ONE, BigDecimal.TEN, currency, BigDecimal.valueOf(1300));
            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(any(BigDecimal.class)))
                    .thenReturn(Optional.of(flatFeePolicy));
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyInactive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.empty());

            // when & then
            CustomException exception = assertThrows(CustomException.class, () -> {
                remittanceFeeService.calculateAndSaveFee(req);
            });
            assertEquals("Network fee not found for currency: " + currency, exception.getMessage());
        }
    }
}
