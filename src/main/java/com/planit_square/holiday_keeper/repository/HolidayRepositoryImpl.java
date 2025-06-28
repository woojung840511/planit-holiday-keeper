package com.planit_square.holiday_keeper.repository;

import com.planit_square.holiday_keeper.dto.HolidaySearchDto;
import com.planit_square.holiday_keeper.entity.Holiday;
import com.planit_square.holiday_keeper.entity.QHoliday;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HolidayRepositoryImpl implements HolidayRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QHoliday holiday = QHoliday.holiday;

    @Override
    public Page<Holiday> findHolidaysWithFilters(HolidaySearchDto searchDto, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (searchDto.hasCountryFilter()) {
            builder.and(holiday.country.in(searchDto.getCountries()));
        }

        if (searchDto.hasYearFilter()) {
            builder.and(holiday.year.in(searchDto.getYears()));
        }

        if (searchDto.getStartDate() != null) {
            builder.and(holiday.date.goe(searchDto.getStartDate()));
        }

        if (searchDto.getEndDate() != null) {
            builder.and(holiday.date.loe(searchDto.getEndDate()));
        }

        Long total = queryFactory
            .select(holiday.count())
            .from(holiday)
            .where(builder)
            .fetchOne();

        List<Holiday> content = queryFactory
            .selectFrom(holiday)
            .where(builder)
            .orderBy(
//                Expressions.stringPath("country_code").asc(),
                holiday.year.desc(),
                holiday.date.asc()
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
