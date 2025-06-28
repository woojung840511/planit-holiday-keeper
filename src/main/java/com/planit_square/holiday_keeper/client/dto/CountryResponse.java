package com.planit_square.holiday_keeper.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryResponse {

    private String countryCode;
    private String name;

}