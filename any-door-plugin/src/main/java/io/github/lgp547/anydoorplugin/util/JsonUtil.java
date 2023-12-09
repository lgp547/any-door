package io.github.lgp547.anydoorplugin.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.JsonArray;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class JsonUtil {

    public static ObjectMapper objectMapper = new ObjectMapper();

    private final static ObjectWriter PRETTY_WRITER = objectMapper.writer().with(new AnyDoorJsonPrettyPrinter());

    /**
     * 压缩 json
     */
    public static String compressJson(String json) {
        if (StringUtils.isBlank(json) || "{}".equals(json)) {
            return json;
        }
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(json);
        } catch (Exception e) {
            return json;
        }
        return jsonNode.toString();
    }

    /**
     * 格式化json
     */
    public static String formatterJson(String json) {
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(json);
            return PRETTY_WRITER.writeValueAsString(jsonNode);
        } catch (Exception e) {
            return json;
        }
    }

    public static JsonArray toJsonArray(List<String> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

    public static String transformedKey(String text, Map<String, String> paramTypeNameTransformer) {
        try {
            JsonNode jsonNode = objectMapper.readTree(text);
            if (jsonNode.isArray()) {
                // 处理 JsonArray
                List<Map<String, Object>> newJsonArray = new ArrayList<>();
                for (JsonNode node : jsonNode) {
                    Map<String, Object> newMap = new HashMap<>();
                    Map<String, Object> map = objectMapper.convertValue(node, Map.class);
                    map.forEach((key, value) -> newMap.put(paramTypeNameTransformer.getOrDefault(key, key), value));
                    newJsonArray.add(newMap);
                }
                return objectMapper.writeValueAsString(newJsonArray);
            } else {
                // 处理 JsonObject
                Map<String, Object> map = objectMapper.readValue(text, Map.class);
                Map<String, Object> newJsonObject = new HashMap<>();
                map.forEach((key, value) -> newJsonObject.put(paramTypeNameTransformer.getOrDefault(key, key), value));
                return objectMapper.writeValueAsString(newJsonObject);
            }
        } catch (Exception ignored) {
        }
        return text;
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
