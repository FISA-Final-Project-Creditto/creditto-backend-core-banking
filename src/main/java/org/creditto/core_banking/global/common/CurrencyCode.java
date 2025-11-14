package org.creditto.core_banking.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurrencyCode {

    KRW("KRW", "원"),
    USD("USD", "미국 달러"),
    JPY("JPY", "일본 엔"),
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
    IDR("IDR", "인도네시아 루피아");
    // VND("VND", "베트남 동"),
    // RUB("RUB", "러시아 루블"),
    // BRL("BRL", "브라질 헤알"),
    // PHP("PHP", "필리핀 페소"),

    private final String code;
    private final String name;
}
