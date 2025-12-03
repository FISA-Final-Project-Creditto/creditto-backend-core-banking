package org.creditto.core_banking.domain.creditscore.service;

import lombok.RequiredArgsConstructor;
import org.creditto.core_banking.domain.creditscore.entity.CreditScore;
import org.creditto.core_banking.domain.creditscore.repository.CreditScoreRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private final CreditScoreRepository creditScoreRepository;

    /**
     * 특정 사용자의 신용점수를 조회하고, 해당 점수에 따른 우대 환율을 반환합니다.
     * 신용점수가 없는 경우 기본 우대율 (0.0)을 반환합니다.
     * @param userId 사용자 ID
     * @return 적용될 우대 환율 (예: 0.9 = 90% 우대)
     20      */
    public double getPreferentialRate(Long userId) {
        // userId로 신용 점수를 찾고, 없으면 기본값으로 처리
        Integer score = creditScoreRepository.findByUserId(userId)
                .map(CreditScore::getScore)
                .orElse(0);// 신용 점수가 없는 경우 0점으로 처리하여 ETC 등급이 적용

        return PreferentialRateTier.getRateForScore(score);
    }
}
