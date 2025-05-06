package org.example.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class Utils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T readJson(String json, final Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
