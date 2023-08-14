package io.github.lgp547.anydoorplugin.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public static String formatterJson(String json) {
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return json;
        }
        return jsonNode.toPrettyString();
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

    /**
     * 将当前json内容转成query参数
     * {
     *     "dto":{
     *         "name":"1",
     *         "phone": 1
     *     },
     *     "id": 1
     * }
     */
    public static String jsonToQuery(String text) {
        try {
            Map<String, Object> map = new HashMap<>();
            Map<String, Object> contentMap = JsonUtil.toMap(text);
            contentMap.forEach((k,v) -> {
                String content = JsonUtil.toStrNotExc(v);
                if (!StringUtils.isBlank(content)) {
                    Map<String, Object> objectMap = JsonUtil.toMap(content);
                    if (objectMap.isEmpty()) {
                        map.put(k, content);
                    } else {
                        map.putAll(objectMap);
                    }
                }
            });

            String queryParam = map.entrySet().stream().filter(entry -> Objects.nonNull(entry.getValue()))
                    .map(entry -> entry.getKey()
                            + "="
                            + URLEncoder.encode(entry.getValue().toString(), Charset.defaultCharset()))
                    .collect(Collectors.joining("&"));
            text = "?" + queryParam;
        } catch (Exception ignored) {
        }
        return text;
    }
}
