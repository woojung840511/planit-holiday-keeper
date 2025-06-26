package com.planit_square.holiday_keeper.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CountryConverter implements AttributeConverter<Country, String> {

    @Override
    public String convertToDatabaseColumn(Country country) {
        return country != null ? country.getCode() : null;
    }

    @Override
    public Country convertToEntityAttribute(String codeFromDatabase) {
        return codeFromDatabase != null ? Country.of(codeFromDatabase) : null;
    }
}
