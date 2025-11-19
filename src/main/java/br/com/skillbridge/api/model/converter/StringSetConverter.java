package br.com.skillbridge.api.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter(autoApply = false)
public class StringSetConverter implements AttributeConverter<Set<String>, String> {

    private static final String DELIMITER = ";";
    private static final String SPLIT_REGEX = "[;,]";

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new LinkedHashSet<>();
        }
        return Arrays.stream(dbData.split(SPLIT_REGEX))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}


