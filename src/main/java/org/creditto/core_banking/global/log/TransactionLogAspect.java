package org.creditto.core_banking.global.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.creditto.core_banking.global.config.TxContext;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class TransactionLogAspect {

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        TxContext.start();
        String txId = TxContext.get();
        log.info("[TX-BEGIN] txId={} method={}", txId, joinPoint.getSignature());

        try {
            Object result = joinPoint.proceed();
            log.info("[TX-COMMIT] txId={} method={}", txId, joinPoint.getSignature());
            TxContext.clear();
            return result;
        } catch (Exception e) {
            log.info("[TX-ROLLBACK] txId={} method={} error={}",
                    txId, joinPoint.getSignature(), e.getMessage());
            TxContext.clear();
            throw e;
        }
    }
}