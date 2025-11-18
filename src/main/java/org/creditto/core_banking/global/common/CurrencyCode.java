package org.creditto.core_banking.global.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.creditto.core_banking.global.response.error.ErrorBaseCode;
import org.creditto.core_banking.global.response.exception.CustomBaseException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum CurrencyCode {

    KRW("KRW", "원"),
    USD("USD", "미국 달러"),
    JPY("JPY", "일본 옌", 100),
    EUR("EUR", "유로"),
    CNY("CNY", "중국 위안"),
    AUD("AUD", "호주 달러"),
    BHD("BHD", "바레인 디나르"),
    HKD("HKD", "홍콩 달러"),
    SGD("SGD", "싱가포르 달러"),
    AED("AED", "아랍에미리트 디르함"),
    GBP("GBP", "영국 파운드"),
    MYR("MYR", "말레이시아 링깃"),
    IDR("IDR", "인도네시아 루피아", 100);

    private static final Pattern UNIT_PATTERN = Pattern.compile("\\(\\d+\\)");

    // 코드 조회 맵 생성
    private static final Map<String, CurrencyCode> CODE_MAP =
            Stream.of(values()).collect(Collectors.toMap(
                    currency -> currency.getCode().toUpperCase(),
                    Function.identity()
            ));

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

    /**
     * 문자열로부터 CurrencyCode Enum 상수를 조회합니다.
     * O(1) 시간 복잡도를 위해 static 맵을 사용합니다.
     * @param curUnit "USD", "JPY(100)" 등의 통화 문자열
     * @return 해당하는 CurrencyCode Enum 상수
     */
    @JsonCreator
    public static CurrencyCode from(String curUnit) {
        if (curUnit == null || curUnit.isEmpty()) {
            throw new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED);
        }

        String codeToFind = parseCurrencyCode(curUnit).toUpperCase();

        return Optional.ofNullable(CODE_MAP.get(codeToFind))
                .orElseThrow(() -> new CustomBaseException(ErrorBaseCode.CURRENCY_NOT_SUPPORTED));
    }

    /**
     * 통화 코드와 단위를 포함하는 조회 키 문자열을 반환합니다.
     * 예를 들어, JPY 통화의 단위가 100인 경우 "JPY(100)"를 반환하고,
     * 단위가 1인 경우 "USD"와 같이 통화 코드만 반환합니다.
     * 이 키는 외부 API 응답 맵에서 해당 통화의 환율 정보를 조회하는 데 사용됩니다.
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