package com.planit_square.holiday_keeper.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.planit_square.holiday_keeper.dto.HolidaySearchDto;
import com.planit_square.holiday_keeper.entity.Country;
import com.planit_square.holiday_keeper.entity.Holiday;
import com.planit_square.holiday_keeper.repository.HolidayRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "logging.level.org.hibernate.SQL=DEBUG"
})
class HolidayServiceTest {

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private HolidayRepository holidayRepository;

    @Test
    void 실제_API로_공휴일_재동기화_테스트() {
        // When - 실제 API 호출해서 한국 2024년 데이터 동기화
        holidayService.refreshHolidays("KR", 2024);

        // 실제 API에서 가져온 데이터 확인
        List<Holiday> holidays = holidayRepository.findByCountryAndYear(Country.of("KR"), 2024);
        assertThat(holidays).isNotEmpty();

        // 신정이 포함되어 있는지 확인
        boolean hasNewYear = holidays.stream()
            .anyMatch(h -> h.getDate().equals(LocalDate.of(2024, 1, 1)));
        assertThat(hasNewYear).isTrue();

        // 데이터 품질 확인
        for (Holiday holiday : holidays) {
            assertThat(holiday.getLocalName()).isNotBlank();
            assertThat(holiday.getName()).isNotBlank();
            assertThat(holiday.getYear()).isEqualTo(2024);
        }
    }

    @Test
    void 실제_API_데이터로_삭제_테스트() {
        // Given - 실제 API에서 데이터 로딩
        holidayService.refreshHolidays("KR", 2024);
        List<Holiday> holidays = holidayRepository.findByCountryAndYear(Country.of("KR"), 2024);
        int loaded = holidays.size();

        // When - 삭제 수행
        int deletedCount = holidayService.deleteHolidays("KR", 2024);

        // Then
        assertThat(deletedCount).isEqualTo(loaded);  // 로딩한 만큼 삭제되어야 함
        assertThat(holidayRepository.findByCountryAndYear(Country.of("KR"), 2024)).isEmpty();
    }

    @Test
    void 날짜_범위_검색_테스트() {
        // Given - 한국 2024년 데이터 로딩
        holidayService.refreshHolidays("KR", 2024);

        // When - 상반기만 검색 (1월~6월)
        HolidaySearchDto searchDto = HolidaySearchDto.builder()
            .countries(List.of(Country.of("KR")))
            .startDate(LocalDate.of(2024, 1, 1))
            .endDate(LocalDate.of(2024, 6, 30))
            .build();

        Page<Holiday> result = holidayService.searchHolidays(searchDto, PageRequest.of(0, 20));

        // Then
        assertThat(result.getContent()).isNotEmpty();

        // 모든 결과가 상반기에 속하는지 확인
        for (Holiday holiday : result.getContent()) {
            assertThat(holiday.getDate()).isBetween(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
            );
        }
    }
}