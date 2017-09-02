package app.domain.convert;


import org.apache.commons.lang.StringUtils;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListToStringConverter implements AttributeConverter<List<String>, String> {

    public static final String SEPARATOR = ",";

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        return attribute != null && !attribute.isEmpty() ? StringUtils.join(attribute, SEPARATOR) : "";
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        return dbData != null && !dbData.isEmpty() ? Arrays.asList(dbData.split(SEPARATOR)) : new ArrayList<>();
    }
}
