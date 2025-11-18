package org.creditto.core_banking.domain.account.service;

import org.creditto.core_banking.domain.transaction.entity.TxnType;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TransactionStrategyFactory {

    // 각 거래 타입에 맞는 전략을 Map 형태로 저장
    private final Map<TxnType, TransactionStrategy> strategyMap;

    /**
     * 생성자에서 스프링이 찾아준 TransactionStrategy 타입의 모든 Bean을 주입 받음
     * 주입받은 리스트를 순회하며 각 전략의 타입을 Key로 하여 Map에 저장
     */
    public TransactionStrategyFactory(List<TransactionStrategy> strategies) {
        strategyMap = new EnumMap<>(TxnType.class);
        strategies.forEach(
                strategy -> strategyMap.put(strategy.getTxnType(), strategy));
    }

    /**
     * Transaction Type에 맞는 전략 구현체를 반환
     * @param txnType 거래 유형
     * @return 해당 거래 유형에 맞는 strategy 객체
     */
    public TransactionStrategy getStrategy(TxnType txnType) {
        TransactionStrategy strategy = strategyMap.get(txnType);
        if (strategy == null) {
            throw new CustomBaseException(ErrorBaseCode.NOT_FOUND_TRANSACTION_TYPE);
        }
        return strategy;
    }
}
