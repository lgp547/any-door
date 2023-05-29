package io.github.lgp547.anydoorplugin.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    public static Gson gson = new GsonBuilder().create();

    public static ObjectMapper objectMapper = new ObjectMapper();

    /**
     * compress json
     */
    public static String compressJson(String json) {
        if (StringUtils.isBlank(json) || "{}".equals(json)) {
            return json;
        }
        return json.replaceAll("\\n", "").replaceAll("\\t", "").replaceAll("\\r", "").replaceAll("\\s", "");
    }

    public static JsonArray toJsonArray(List<String> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

    public static String transformedKey(String text, Map<String, String> paramTypeNameTransformer) {
        JsonObject jsonObject = gson.fromJson(text, JsonObject.class);
        JsonObject newJsonObj = new JsonObject();
        jsonObject.entrySet().forEach(entry -> newJsonObj.add(paramTypeNameTransformer.getOrDefault(entry.getKey(), entry.getKey()), entry.getValue()));
        return gson.toJson(newJsonObj);
    }

    public static String toStr(Object value) throws Exception {
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return objectMapper.writeValueAsString(value);
        }
    }

    public static String toStrNotExc(Object value) {
        try {
            return toStr(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T toJavaBean(String content, Class<T> tClass) throws Exception {
        return objectMapper.readValue(content, tClass);
    }

    public static Map<String, Object> toMap(String content) {
        try {
            TypeReference<Map<String, Object>> typeReference = new TypeReference<>() {
            };
            return objectMapper.readValue(content, typeReference);
        } catch (Exception ignored) {
        }
        return new HashMap<>();
    }
}
