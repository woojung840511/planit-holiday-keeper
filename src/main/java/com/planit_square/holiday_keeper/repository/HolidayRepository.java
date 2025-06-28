package com.planit_square.holiday_keeper.repository;

import com.planit_square.holiday_keeper.entity.Country;
import com.planit_square.holiday_keeper.entity.Holiday;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    // 재동기화 (upsert) 를 위해 특정 국가 연도 휴일 목록 조회
    List<Holiday> findByCountryAndYear(Country country, Integer year);

    @Modifying
    @Query("DELETE FROM Holiday h WHERE h.country = :country AND h.year = :year")
    int deleteByCountryAndYear(Country country, Integer year);

    // 특정 국가와 연도의 휴일이 존재하는지 확인 (초기 데이터 적재나 재동기화 시 사용)
    boolean existsByCountryAndYear(Country country, Integer year);

}
