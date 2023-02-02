package io.github.lgp547.anydoor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class JsonUtil {

    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T toJavaBean(String content, Class<T> valueType) {
        try {
            return objectMapper.readValue(content, valueType);
        } catch (Exception e) {
            log.debug("toJavaBean exception ", e);
            throw new IllegalArgumentException(e);
        }
    }

    public static Map<String, Object> toMap(String content) {
        try {
            return objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            log.debug("toMap exception ", e);
            throw new IllegalArgumentException(e);
        }
    }

    public static String toStrNotExc(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                log.debug("toStrNotExc writeValueAsString ", e);
                return null;
            }
        }
    }
}
