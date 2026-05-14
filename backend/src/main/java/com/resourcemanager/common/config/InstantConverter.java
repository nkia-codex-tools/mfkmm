package com.resourcemanager.common.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;

@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, String> {

    @Override
    public String convertToDatabaseColumn(Instant attribute) {
        return attribute != null ? attribute.toString() : null;
    }

    @Override
    public Instant convertToEntityAttribute(String dbData) {
        return dbData != null && !dbData.isBlank() ? Instant.parse(dbData) : null;
    }
}
