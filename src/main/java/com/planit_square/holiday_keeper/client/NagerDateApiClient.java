package com.planit_square.holiday_keeper.client;

import com.planit_square.holiday_keeper.client.dto.CountryResponse;
import com.planit_square.holiday_keeper.client.dto.HolidayResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class NagerDateApiClient {

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://date.nager.at/api/v3";
    private static final String COUNTRIES_ENDPOINT = BASE_URL + "/AvailableCountries";
    private static final String HOLIDAYS_ENDPOINT = BASE_URL + "/PublicHolidays/{year}/{countryCode}";

    public List<CountryResponse> getAvailableCountries() {
        try {

            ResponseEntity<List<CountryResponse>> response = restTemplate.exchange(
                COUNTRIES_ENDPOINT,
                HttpMethod.GET,
                null, // HttpEntity (요청 바디나 헤더)
                new ParameterizedTypeReference<>() {}
            );

            return response.getBody();

        } catch (RestClientException e) {
            throw new ExternalApiException("❌ 국가 목록 조회 실패", e);
        }
    }

    public List<HolidayResponse> getPublicHolidays(int year, String countryCode) {
        try {

            ResponseEntity<List<HolidayResponse>> response = restTemplate.exchange(
                HOLIDAYS_ENDPOINT,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {},
                year,
                countryCode.toUpperCase()
            );

            List<HolidayResponse> holidays = response.getBody();
            log.info("✅ 휴일 목록 조회 성공: {} 개의 휴일 ({}-{})",
                holidays != null ? holidays.size() : 0, countryCode, year);

            return holidays;

        } catch (RestClientException e) {
            // 특정 국가-연도 조합에서 데이터가 없을 수 있으므로 예외를 던지지 않고 빈 리스트 반환
            return List.of();
        }
    }
}