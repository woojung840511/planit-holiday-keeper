package com.planit_square.holiday_keeper.config;

import com.planit_square.holiday_keeper.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HolidayDataLoader implements ApplicationRunner {

    private final HolidayService holidayService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("초기 데이터 확인 중...");

        try {
            holidayService.loadInitialHolidayData();
        } catch (Exception e) {
            log.error("❌ 공휴일 데이터 초기화 실패", e);
            // 애플리케이션을 중단시키지 않고 계속 실행 (수동으로 데이터 로딩 가능)
        }
    }
}