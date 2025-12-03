package org.creditto.core_banking.domain.creditscore.service;

import lombok.Getter;

/**
 * 신용 점수 별 우대율 정의
 */
@Getter
public enum PreferentialRateTier {

    SCORE_900(900, 0.8),

    SCORE_800(800, 0.7),

    SCORE_700(700, 0.6),

    SCORE_600(600, 0.55),

    ETC (0, 0.5);

    private final int minScore;
    private final double preferentialRate;

    PreferentialRateTier(int minScore, double preferentialRate) {
        this.minScore = minScore;
        this.preferentialRate = preferentialRate;
    }

    public static double getRateForScore(int score) {
        if (score >= SCORE_900.minScore) {
            return SCORE_900.preferentialRate;
        }

        if (score >= SCORE_800.minScore) {
            return SCORE_800.preferentialRate;
        }

        if (score >= SCORE_700.minScore) {
            return SCORE_700.preferentialRate;
        }

        if (score >= SCORE_600.minScore) {
            return SCORE_600.preferentialRate;
        }

        return ETC.preferentialRate;
    }
}
