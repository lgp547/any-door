package io.github.lgp547.anydoor.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.lgp547.anydoor.util.jackson.AnyDoorTimeDeserializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JsonUtil {

    public static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //序列化处理
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        objectMapper.configure(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.mappedFeature(), true);
        //失败处理
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //单引号处理
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //时间处理
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new AnyDoorTimeDeserializer());
        objectMapper.registerModule(javaTimeModule);
    }

    public static <T> T toJavaBean(String content, Type valueType) {
        try {
            JavaType javaType = JsonUtil.objectMapper.getTypeFactory().constructType(valueType);
            if (javaType.isTypeOrSubTypeOf(Temporal.class)) {
                content = "\"" + content + "\"";
            }
            return objectMapper.readValue(content, javaType);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static Map<String, Object> toMap(String content) {
        try {
            return objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static List<Map<String, Object>> toMaps(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            List<Map<String, Object>> result = new ArrayList<>();
            for (JsonNode node : jsonNode) {
                Map<String, Object> map = objectMapper.convertValue(node, Map.class);
                result.add(map);
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isJsonArray(String jsonString) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            return jsonNode.isArray();
        } catch (Exception e) {
            return false;
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
            } catch (Throwable e) {
                return null;
            }
        }
    }

    public static String toContentStrNotExc(Object value) {
        if (value instanceof Optional) {
            Optional<?> valueOpt = (Optional<?>) value;
            if (valueOpt.isPresent()) {
                return toStrNotExc(valueOpt.get());
            }
        }
        return toStrNotExc(value);
    }
}
