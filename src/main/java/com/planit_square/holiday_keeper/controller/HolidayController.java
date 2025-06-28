package com.planit_square.holiday_keeper.controller;

import com.planit_square.holiday_keeper.dto.HolidaySearchDto;
import com.planit_square.holiday_keeper.entity.Country;
import com.planit_square.holiday_keeper.entity.Holiday;
import com.planit_square.holiday_keeper.service.HolidayService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @GetMapping
    public ResponseEntity<Page<Holiday>> getHolidays(
        @RequestParam(required = false) List<String> countries,
        @RequestParam(required = false) List<Integer> years,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @PageableDefault(size = 20) Pageable pageable) {

        List<Country> countryEntities = countries != null ?
            countries.stream().map(Country::of).toList() : null;

        HolidaySearchDto searchDto = HolidaySearchDto.builder()
            .countries(countryEntities)
            .years(years)
            .startDate(startDate)
            .endDate(endDate)
            .build();

        Page<Holiday> result = holidayService.searchHolidays(searchDto, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/initialize")
    public ResponseEntity<Map<String, String>> initializeHolidays() {
        holidayService.loadInitialHolidayData();
        return ResponseEntity.ok(Map.of("message", "공휴일 데이터 초기화 완료"));
    }

    @PostMapping("/{countryCode}/{year}/sync")
    public ResponseEntity<Map<String, Object>> syncHolidays(
        @PathVariable String countryCode,
        @PathVariable Integer year) {

        int changes = holidayService.refreshHolidays(countryCode.toUpperCase(), year);
        return ResponseEntity.ok(Map.of(
            "message", "동기화 완료",
            "changes", changes
        ));
    }

    @DeleteMapping("/{countryCode}/{year}")
    public ResponseEntity<Map<String, Object>> deleteHolidays(
        @PathVariable String countryCode,
        @PathVariable Integer year) {

        int deletedCount = holidayService.deleteHolidays(countryCode.toUpperCase(), year);
        return ResponseEntity.ok(Map.of(
            "message", "삭제 완료",
            "deletedCount", deletedCount
        ));
    }
}