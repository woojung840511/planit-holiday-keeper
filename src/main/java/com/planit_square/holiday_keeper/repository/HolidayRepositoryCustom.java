package com.planit_square.holiday_keeper.repository;

import com.planit_square.holiday_keeper.dto.HolidaySearchDto;
import com.planit_square.holiday_keeper.entity.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HolidayRepositoryCustom {

    Page<Holiday> findHolidaysWithFilters(HolidaySearchDto searchDto, Pageable pageable);

}
