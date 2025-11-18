package org.creditto.core_banking.global.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;

import java.util.regex.Pattern;

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

    private static final Pattern UNIT_PATTERN = Pattern.compile("\\(\\d+\\)");

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

    /**
     * 원본 통화 문자열(예: "JPY(100)")을 파싱하여 정제된 통화 코드(예: "JPY")를 반환합니다.
     * @param rawCode API에서 받은 원본 통화 문자열
     * @return 정제된 통화 코드
     */
    public static String parseCurrencyCode(String rawCode) {
        if (rawCode == null) {
            return null;
        }
        return UNIT_PATTERN.matcher(rawCode).replaceAll("").trim();
    }

    @JsonCreator
    public static CurrencyCode from(String curUnit) {
        if (curUnit == null) {
            return null;
        }

        String codeToFind = parseCurrencyCode(curUnit);

        for (CurrencyCode code : values()) {
            if (code.getCode().equalsIgnoreCase(codeToFind)) {
                return code;
            }
            if (code.name().equalsIgnoreCase(codeToFind)) {
                return code;
            }
        }
        throw new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
    }

    /**
     * 통화 코드와 단위를 포함하는 조회 키 문자열을 반환
     * 예를 들어, JPY 통화의 단위가 100인 경우 "JPY(100)"를 반환하고,
     * 단위가 1인 경우 "USD"와 같이 통화 코드만 반환
     * 이 키는 외부 API 응답 맵에서 해당 통화의 환율 정보를 조회하는 데 사용됨
     * @return 통화 조회에 사용될 형식화된 키 문자열
     */
    public String getLookupKey() {
        String lookupKey = this.code.toUpperCase();
        if (this.unit > 1) {
            lookupKey += "(" + this.unit + ")";
        }
        return lookupKey;
    }
}
