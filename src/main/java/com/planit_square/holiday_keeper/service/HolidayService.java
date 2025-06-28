package com.planit_square.holiday_keeper.service;

import com.planit_square.holiday_keeper.client.NagerDateApiClient;
import com.planit_square.holiday_keeper.client.dto.CountryResponse;
import com.planit_square.holiday_keeper.client.dto.HolidayResponse;
import com.planit_square.holiday_keeper.dto.HolidaySearchDto;
import com.planit_square.holiday_keeper.entity.Country;
import com.planit_square.holiday_keeper.entity.Holiday;
import com.planit_square.holiday_keeper.repository.HolidayRepository;
import com.planit_square.holiday_keeper.repository.HolidayRepositoryCustom;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HolidayService {

    private final NagerDateApiClient nagerDateApiClient;
    private final HolidayRepository holidayRepository;
    private final HolidayRepositoryCustom holidayRepositoryCustom;

    // 최근 5년
    private static final int RECENT_YEARS = 5;

    private int getStartYear() {
        return LocalDate.now().getYear() - RECENT_YEARS + 1;
    }

    private int getEndYear() {
        return LocalDate.now().getYear();
    }

    public void loadInitialHolidayData() {
        int startYear = getStartYear();
        int endYear = getEndYear();
        log.info("최근 {}년간 공휴일 데이터 로딩 시작: {}-{}", RECENT_YEARS, startYear, endYear);

        try {
            List<CountryResponse> countries = nagerDateApiClient.getAvailableCountries();
            log.info("국가 수: {}", countries.size());

            updateCountryCache(countries);

            int totalLoaded = 0;
            int successfulCountries = 0;

            for (CountryResponse countryResponse : countries) {

                String countryCode = countryResponse.getCountryCode();
                try {
                    int countryHolidays = loadHolidaysForCountry(countryCode);
                    totalLoaded += countryHolidays;
                    successfulCountries++;

                    log.debug("✅ {} 공휴일 {} 개 로딩 완료", countryCode, countryHolidays);

                } catch (Exception e) {
                    log.warn("❌ {} 공휴일 로딩 실패: {}", countryCode, e.getMessage());
                }
            }

            log.info("초기 데이터 로딩 완료: 총 {} 개 공휴일을 {}/{} 개 국가에서 로딩",
                totalLoaded, successfulCountries, countries.size());

        } catch (Exception e) {
            String errorInfo = "❌ 초기 데이터 로딩 실패";
            log.error(errorInfo, e);
            throw new RuntimeException(errorInfo, e);
        }
    }

    private int loadHolidaysForCountry(String countryCode) {
        int totalHolidays = 0;
        int startYear = getStartYear();
        int endYear = getEndYear();

        for (int i = startYear; i <= endYear; i++) {

            if (holidayRepository.existsByCountryAndYear(Country.of(countryCode), i)) {
                log.info("{}-{} 데이터 이미 존재하여 스킵", countryCode, i);
                continue;
            }

            List<HolidayResponse> apiHolidays = nagerDateApiClient.getPublicHolidays(i, countryCode);
            List<Holiday> holidays = convertToEntities(apiHolidays, countryCode);

            if (!holidays.isEmpty()) {
                holidayRepository.saveAll(holidays);
                totalHolidays += holidays.size();
            }
        }

        return totalHolidays;
    }

    @Transactional(readOnly = true)
    public Page<Holiday> searchHolidays(HolidaySearchDto searchDto, Pageable pageable) {
        log.info("공휴일 검색: 국가={}, 연도={}, 날짜범위={}-{}",
            searchDto.hasCountryFilter() ? searchDto.getCountries().size() : "전체",
            searchDto.hasYearFilter() ? searchDto.getYears().size() : "전체",
            searchDto.getStartDate(), searchDto.getEndDate());

        return holidayRepositoryCustom.findHolidaysWithFilters(searchDto, pageable);
    }

    @Transactional
    public int refreshHolidays(String countryCode, int year) {
        log.info("{}-{} 공휴일 재동기화 시작", countryCode, year);

        try {
            Country country = Country.of(countryCode);

            // 1. 외부 API에서 최신 데이터 조회
            List<HolidayResponse> apiHolidays = nagerDateApiClient.getPublicHolidays(year, countryCode);
            List<Holiday> newHolidays = convertToEntities(apiHolidays, countryCode);

            // 2. 기존 데이터 조회
            List<Holiday> existingHolidays = holidayRepository.findByCountryAndYear(country, year);

            // 3. Upsert 로직 수행
            int changes = performUpsert(existingHolidays, newHolidays);

            log.info("✅ {}-{} 재동기화 완료: {} 건 변경", countryCode, year, changes);
            return changes;

        } catch (Exception e) {
            log.error("❌ {}-{} 공휴일 재동기화 실패", countryCode, year, e);
            throw new RuntimeException("공휴일 재동기화 실패", e);
        }
    }

    private int performUpsert(List<Holiday> existing, List<Holiday> newData) {
        int changes = 0;

        // 업데이트 및 신규 추가
        for (Holiday newHoliday : newData) {
            Holiday existingHoliday = existing.stream()
                .filter(h -> h.isSameHoliday(newHoliday))
                .findFirst()
                .orElse(null);

            if (existingHoliday != null) {
                // 기존 데이터 업데이트
                if (!existingHoliday.getLocalName().equals(newHoliday.getLocalName()) ||
                    !existingHoliday.getName().equals(newHoliday.getName())) {

                    existingHoliday.updateInfo(newHoliday.getLocalName(), newHoliday.getName());
                    changes++;
                }
            } else {
                // 신규 데이터 추가
                holidayRepository.save(newHoliday);
                changes++;
            }
        }

        // 삭제: API에서 제공하지 않는 데이터 제거
        for (Holiday existingHoliday : existing) {
            boolean existsInNewData = newData.stream()
                .anyMatch(h -> h.isSameHoliday(existingHoliday));

            if (!existsInNewData) {
                holidayRepository.delete(existingHoliday);
                changes++;
            }
        }

        return changes;
    }

    public int deleteHolidays(String countryCode, int year) {
        log.info("{}-{} 공휴일 삭제 시작", countryCode, year);

        try {
            Country country = Country.of(countryCode);
            int deleteCount = holidayRepository.deleteByCountryAndYear(country, year);

            log.info("✅ {}-{} 공휴일 {} 건 삭제 완료", countryCode, year, deleteCount);
            return deleteCount;

        } catch (Exception e) {
            log.error("❌ {}-{} 공휴일 삭제 실패", countryCode, year, e);
            throw new RuntimeException("공휴일 삭제 실패", e);
        }
    }

    private void updateCountryCache(List<CountryResponse> countries) {
        try {
            Map<String, String> countryMap = countries.stream()
                .collect(Collectors.toMap(
                    CountryResponse::getCountryCode,
                    CountryResponse::getName
                ));

            Country.updateCache(countryMap);
            log.info("✅ 국가 캐시 업데이트 완료: {} 개국", countryMap.size());

        } catch (Exception e) {
            log.warn("❌ 국가 캐시 업데이트 실패, 기본값 사용", e);
            Country.restoreToFallback();
        }
    }

    private List<Holiday> convertToEntities(List<HolidayResponse> responses, String countryCode) {
        Country country = Country.of(countryCode);

        return responses.stream()
            .filter(response -> response.getDate() != null)
            .filter(response -> response.getName() != null && !response.getName().isEmpty())
            .filter(response -> response.getLocalName() != null && !response.getLocalName().isEmpty())
            .map(response -> Holiday.builder()
                .date(response.getDate())
                .localName(response.getLocalName())
                .name(response.getName())
                .country(country)
                .build())
            .collect(Collectors.toList());
    }

}