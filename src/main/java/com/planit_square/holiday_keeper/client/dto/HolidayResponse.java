package com.planit_square.holiday_keeper.client.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HolidayResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String localName;

    private String name;

    private String countryCode;

}