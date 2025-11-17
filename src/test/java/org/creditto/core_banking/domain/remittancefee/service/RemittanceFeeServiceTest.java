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

    private FlatServiceFee flatFeePolicy;
    private PctServiceFee pctFeePolicyInactive;
    private PctServiceFee pctFeePolicyActive;
    private NetworkFee networkFeePolicyUSD;
    private NetworkFee networkFeePolicyJPY;
    private NetworkFee networkFeePolicyCNY;

    @BeforeEach
    void setUp() {
        // Mock 정책 데이터 설정
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
            BigDecimal sendAmount = new BigDecimal("1000");
            BigDecimal exchangeRate = new BigDecimal("1300");
            String currency = "USD";
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency);

            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmount))
                .thenReturn(Optional.of(flatFeePolicy));
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyInactive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyUSD));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 7500
            // pctFeeInKRW = 0 (비활성)
            // networkFeeInKRW = 15 * 1300 = 19500
            // totalFee = 7500 + 0 + 19500 = 27000
            BigDecimal expectedTotalFee = new BigDecimal("27000");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }

        @Test
        @DisplayName("JPY 송금 (PctFee 비활성)")
        void calculateAndSaveFee_ForJPY_When_PctFeeInactive() {
            // given
            BigDecimal sendAmount = new BigDecimal("100000"); // 10만엔
            BigDecimal exchangeRate = new BigDecimal("950"); // 100엔 = 950원
            String currency = "JPY";
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency);

            // JPY -> USD 변환: 100000 * 0.0065 = 650
            BigDecimal sendAmountInUSD = sendAmount.multiply(new BigDecimal("0.0065"));

            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmountInUSD))
                .thenReturn(Optional.of(flatFeePolicy));
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyInactive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyJPY));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 7500
            // pctFeeInKRW = 0 (비활성)
            // networkFeeInKRW = (2000 / 100) * 950 = 19000
            // totalFee = 7500 + 0 + 19000 = 7680
            BigDecimal expectedTotalFee = new BigDecimal("26500");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }

        @Test
        @DisplayName("CNY 송금 (PctFee 비활성)")
        void calculateAndSaveFee_ForCNY_When_PctFeeInactive() {
            // given
            BigDecimal sendAmount = new BigDecimal("5000"); // 5000위안
            BigDecimal exchangeRate = new BigDecimal("204"); // 1위안 = 180원
            String currency = "CNY";
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency);

            // CNY -> USD 변환: 5000 * 0.13 = 650
            BigDecimal sendAmountInUSD = sendAmount.multiply(new BigDecimal("0.13"));

            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmountInUSD))
                .thenReturn(Optional.of(flatFeePolicy));
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyInactive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyCNY));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 7500
            // pctFeeInKRW = 0 (비활성)
            // networkFeeInKRW = 100 * 204 = 20400
            // totalFee = 7500 + 0 + 20400 = 27900
            BigDecimal expectedTotalFee = new BigDecimal("27900");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }

        @Test
        @DisplayName("USD 송금 (PctFee 활성)")
        void calculateAndSaveFee_ForUSD_When_PctFeeActive() {
            // given
            BigDecimal sendAmount = new BigDecimal("1000");
            BigDecimal exchangeRate = new BigDecimal("1300");
            String currency = "USD";
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency);

            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmount))
                .thenReturn(Optional.of(flatFeePolicy));
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyActive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyUSD));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 7500
            // pctFeeInKRW = 1000 * (0.2 / 100) * 1300 = 2600
            // networkFeeInKRW = 15 * 1300 = 19500
            // totalFee = 7500 + 2600 + 19500 = 29600
            BigDecimal expectedTotalFee = new BigDecimal("29600");
            assertThat(savedFeeRecord.getTotalFee()).isEqualByComparingTo(expectedTotalFee);
        }

        @Test
        @DisplayName("JPY 송금 (PctFee 활성)")
        void calculateAndSaveFee_ForJPY_When_PctFeeActive() {
            // given
            BigDecimal sendAmount = new BigDecimal("100000"); // 10만엔
            BigDecimal exchangeRate = new BigDecimal("9"); // 1엔 = 9원
            String currency = "JPY";
            RemittanceFeeReq req = new RemittanceFeeReq(exchangeRate, sendAmount, currency);

            BigDecimal sendAmountInUSD = sendAmount.multiply(new BigDecimal("0.0065"));

            when(flatServiceFeeRepository.findFirstByUpperLimitGreaterThanEqualOrderByUpperLimitAsc(sendAmountInUSD))
                .thenReturn(Optional.of(flatFeePolicy));
            when(pctServiceFeeRepository.findFirstByOrderByPctServiceFeeIdAsc()).thenReturn(Optional.of(pctFeePolicyActive));
            when(networkFeeRepository.findByCurrencyCode(currency)).thenReturn(Optional.of(networkFeePolicyJPY));
            when(feeRecordRepository.save(any(FeeRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            remittanceFeeService.calculateAndSaveFee(req);

            // then
            ArgumentCaptor<FeeRecord> feeRecordCaptor = ArgumentCaptor.forClass(FeeRecord.class);
            verify(feeRecordRepository).save(feeRecordCaptor.capture());
            FeeRecord savedFeeRecord = feeRecordCaptor.getValue();

            // flatFeeInKRW = 7500
            // pctFeeInKRW = (100000 / 100) * (0.2 / 100) * 9 = 18
            // networkFeeInKRW = (2000 / 100) * 9 = 180
            // totalFee = 7500 + 18 + 180 = 7698
            BigDecimal expectedTotalFee = new BigDecimal("7698");
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
            RemittanceFeeReq req = new RemittanceFeeReq(BigDecimal.ONE, BigDecimal.TEN, "USD");
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
            RemittanceFeeReq req = new RemittanceFeeReq(BigDecimal.ONE, BigDecimal.TEN, "USD");
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
            RemittanceFeeReq req = new RemittanceFeeReq(BigDecimal.ONE, BigDecimal.TEN, currency);
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
