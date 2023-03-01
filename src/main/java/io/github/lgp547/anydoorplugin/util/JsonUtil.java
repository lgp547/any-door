package io.github.lgp547.anydoorplugin.util;

import com.google.gson.JsonArray;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class JsonUtil {

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
}
