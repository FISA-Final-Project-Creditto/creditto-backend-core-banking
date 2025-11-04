package org.creditto.core_banking.domain.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.creditto.core_banking.domain.transaction.entity.Transaction;
import org.creditto.core_banking.domain.transaction.entity.TxnType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {

    private Long id;
    private BigDecimal txnAmount;
    private TxnType txnType;
    private BigDecimal balanceAfter;
    private LocalDateTime txnAt;

    public static TransactionResponseDto from(Transaction txn) {
        return TransactionResponseDto.builder()
                .id(txn.getId())
                .txnAmount(txn.getTxnAmount())
                .txnType(txn.getTxnType())
                .balanceAfter(txn.getBalanceAfter())
                .txnAt(txn.getTxnAt())
                .build();
    }

}
