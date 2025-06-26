package com.planit_square.holiday_keeper.dto;

import com.planit_square.holiday_keeper.entity.Country;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HolidaySearchDto {

    private List<Country> countries;
    private List<Integer> years;
    private LocalDate startDate;
    private LocalDate endDate;

    public boolean hasCountryFilter() {
        return countries != null && !countries.isEmpty();
    }

    public boolean hasYearFilter() {
        return years != null && !years.isEmpty();
    }

}
