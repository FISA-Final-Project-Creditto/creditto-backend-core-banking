package org.creditto.core_banking.global.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum CurrencyCode {

    KRW("KRW", "원"),
    USD("USD", "미국 달러"),
    JPY("JPY", "일본 옌", 100),
    EUR("EUR", "유로"),
    CNY("CNY", "중국 위안"),
    AUD("AUD", "호주 달러"),
    // BDT("BDT", "방글라데시 타카"),
    BHD("BHD", "바레인 디나르"),
    HKD("HKD", "홍콩 달러"),
    SGD("SGD", "싱가포르 달러"),
    // MMK("MMK", "미얀마 짯"),
    AED("AED", "아랍에미리트 디르함"),
    GBP("GBP", "영국 파운드"),
    MYR("MYR", "말레이시아 링깃"),
    // PLN("PLN", "폴란드 즈워티"),
    // KHR("KHR", "캄보디아 리엘"),
    IDR("IDR", "인도네시아 루피아", 100);
    // VND("VND", "베트남 동"),
    // RUB("RUB", "러시아 루블"),
    // BRL("BRL", "브라질 헤알"),
    // PHP("PHP", "필리핀 페소"),

    private final String code;
    private final String name;
    private final int unit;

    CurrencyCode(String code, String name) {
        this(code, name, 1);
    }

    CurrencyCode(String code, String name, int unit) {
        this.code = code;
        this.name = name;
        this.unit = unit;
    }

    @JsonCreator
    public static CurrencyCode from(String curUnit) {
        if (curUnit == null) {
            return null;
        }
        
        String codeToFind = curUnit.replaceAll("\\(\\d+\\)", "").trim();

        for (CurrencyCode code : values()) {
            if (code.getCode().equalsIgnoreCase(codeToFind)) {
                return code;
            }
            if (code.name().equalsIgnoreCase(codeToFind)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Unknown currency code: " + curUnit);
    }
}
