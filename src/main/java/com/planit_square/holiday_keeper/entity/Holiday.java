package com.planit_square.holiday_keeper.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "holidays",
    indexes = {
        @Index(name = "idx_country_year", columnList = "country_code, holiday_year"), // 국가별 연도 검색용
        @Index(name = "idx_country_date", columnList = "country_code, date") // 국가별 날짜 범위 검색용
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String localName;

    @Column(nullable = false)
    private String name;

    @Column(name = "country_code", nullable = false, length = 2)
    @Convert(converter = CountryConverter.class)
    private Country country;

    @Column(name = "holiday_year", nullable = false)
    private Integer year; // 검색 성능용

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Holiday(LocalDate date, String localName, String name, Country country) {
        this.date = date;
        this.localName = localName;
        this.name = name;
        this.country = country;
        this.year = date.getYear(); // 자동 계산
    }

    // Upsert를 위한 비즈니스 키 비교
    public boolean isSameHoliday(Holiday other) {
        return this.date.equals(other.date)
            && this.country.equals(other.country);
    }

    public void updateInfo(String localName, String name) {
        this.localName = localName;
        this.name = name;
    }
}