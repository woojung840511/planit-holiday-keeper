package com.planit_square.holiday_keeper.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@EqualsAndHashCode
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Country {

    private String code;
    private String name;

    // 런타임 캐시 (API에서 로드된 최신 데이터)
    private static final Map<String, String> RUNTIME_CACHE = new ConcurrentHashMap<>();

    // 정적 캐시 (애플리케이션 시작 시 로드)
    private static final Map<String, String> FALLBACK_COUNTRIES = new HashMap<>() {{
        put("AD", "Andorra");
        put("AL", "Albania");
        put("AM", "Armenia");
        put("AR", "Argentina");
        put("AT", "Austria");
        put("AU", "Australia");
        put("BE", "Belgium");
        put("BG", "Bulgaria");
        put("BR", "Brazil");
        put("CA", "Canada");
        put("CH", "Switzerland");
        put("CL", "Chile");
        put("CN", "China");
        put("CO", "Colombia");
        put("CR", "Costa Rica");
        put("CZ", "Czechia");
        put("DE", "Germany");
        put("DK", "Denmark");
        put("ES", "Spain");
        put("FI", "Finland");
        put("FR", "France");
        put("GB", "United Kingdom");
        put("GR", "Greece");
        put("HU", "Hungary");
        put("IE", "Ireland");
        put("IT", "Italy");
        put("JP", "Japan");
        put("KR", "South Korea");
        put("MX", "Mexico");
        put("NL", "Netherlands");
        put("NO", "Norway");
        put("PL", "Poland");
        put("PT", "Portugal");
        put("RU", "Russia");
        put("SE", "Sweden");
        put("US", "United States");
        put("ZA", "South Africa");
    }};

    static {
        // 런타임 캐시 초기화: 정적 캐시에서 데이터를 복사
        RUNTIME_CACHE.putAll(FALLBACK_COUNTRIES);
        log.info("국가 캐시 초기화 완료: {} 개국", FALLBACK_COUNTRIES.size());
    }

    public static Country of(String code) {
        String validatedCode = validateAndNormalize(code);
        String resolvedName = RUNTIME_CACHE.get(code.toUpperCase());

        return new Country(validatedCode, resolvedName);
    }

    private static String validateAndNormalize(String code) {
        if (code == null || code.length() != 2) {
            log.error("잘못된 국가 코드: {}", code);
            throw new IllegalArgumentException("Country code must be exactly 2 characters long");
        }
        return code.toUpperCase();
    }

    public static void updateCache(Map<String, String> apiData) {
        if (apiData != null && !apiData.isEmpty()) {
            RUNTIME_CACHE.clear();
            RUNTIME_CACHE.putAll(apiData);
            log.info("✅ 국가 캐시 업데이트 완료: {} 개국", apiData.size());
        } else {
            log.warn("⚠️ API 데이터 비어있음");
        }
    }

    public static void restoreToFallback() {
        RUNTIME_CACHE.clear();
        RUNTIME_CACHE.putAll(FALLBACK_COUNTRIES);
        log.info("🔄 국가 캐시를 기본값으로 복원: {} 개국", FALLBACK_COUNTRIES.size());
    }

}