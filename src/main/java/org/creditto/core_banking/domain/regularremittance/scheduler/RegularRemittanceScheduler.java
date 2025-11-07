package org.creditto.core_banking.domain.regularremittance.scheduler;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.regularremittance.service.RegularRemittanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegularRemittanceScheduler {

    private final RegularRemittanceService regularRemittanceService;

    // cron = "초 분 시 일 월 요일"
    @Scheduled(cron = "0 0 1 * * *")    // 매일 새벽 1시 실행
    public void runDailyRemittanceProcessing() {
        System.out.println("정기 송금 처리");
        regularRemittanceService.processDueRemittances();
        System.out.println("완료");
    }

    // 메인 application에 @EnableScheduling 추가해야함!

}
