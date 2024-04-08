package io.github.lgp547.anydoorplugin.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypes;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.impl.source.tree.PsiErrorElementImpl;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class JsonElementUtil {
    public static boolean isNoSupportType(PsiClass psiClass) {
        return psiClass.isEnum();
    }

    public static JsonElement toJson(PsiType type, Integer num) {
        if (num > 5) {
            return JsonNull.INSTANCE;
        }
        // 新版本使用：PsiTypes.intType()
        if (type.isAssignableFrom(PsiTypes.intType())) {
            return new JsonPrimitive(0);
        }
        if (type.isAssignableFrom(PsiTypes.longType())) {
            return new JsonPrimitive(0);
        }
        if (type.isAssignableFrom(PsiTypes.booleanType())) {
            return new JsonPrimitive(false);
        }
        if (type.isAssignableFrom(PsiTypes.byteType())) {
            return new JsonPrimitive("");
        }
        if (type.isAssignableFrom(PsiTypes.charType())) {
            return new JsonPrimitive("");
        }
        if (type.isAssignableFrom(PsiTypes.doubleType())) {
            return new JsonPrimitive(0.00);
        }
        if (type.isAssignableFrom(PsiTypes.floatType())) {
            return new JsonPrimitive(0.0);
        }
        if (type.isAssignableFrom(PsiTypes.shortType())) {
            return new JsonPrimitive("");
        }
        if (type instanceof PsiClassType) {
            PsiClass psiClass = ((PsiClassType) type).resolve();
            if (null != psiClass) {
                if (isNoSupportType(psiClass)) {
                    return JsonNull.INSTANCE;
                } else {
                    try {
                        Class<?> aClass = Class.forName(psiClass.getQualifiedName());
                        if (isSimpleValueType(aClass)) {
                            return new JsonPrimitive("");
                        }
                        if (isCollType(aClass)) {
                            JsonArray jsonElements = new JsonArray();
                            Arrays.stream(((PsiClassType) type).getParameters()).map(i -> toJson(i, num + 1)).forEach(jsonElements::add);
                            return jsonElements;
                        }
                        if (isMapType(aClass)) {
                            JsonObject jsonObject = new JsonObject();
                            PsiType[] parameters = ((PsiClassType) type).getParameters();
                            if (parameters.length > 1 && num < 5) {
                                JsonElement key = toJson(parameters[0], num + 1);
                                JsonElement value = toJson(parameters[1], num + 1);
                                jsonObject.add(key instanceof JsonPrimitive ? key.getAsString() : key.toString(), value);
                            }
                            return jsonObject;
                        }
                    } catch (Exception ignored) {
                    }
                    if (psiClass.isInterface()) {
                        return JsonNull.INSTANCE;
                    }
                    JsonObject jsonObject1 = new JsonObject();
                    Arrays.stream(psiClass.getFields()).forEach(field -> {
                        if (!StringUtils.contains(field.getText(), " static ") && num < 5) {
                            jsonObject1.add(field.getName(), toJson(field.getType(), num + 1));
                        }
                    });
                    return jsonObject1;
                }
            }
        }


        return JsonNull.INSTANCE;
    }

    public static boolean isSimpleValueType(Class<?> type) {
        return (Void.class != type && void.class != type &&
                (ClassUtils.isPrimitiveOrWrapper(type) ||
                        Enum.class.isAssignableFrom(type) ||
                        CharSequence.class.isAssignableFrom(type) ||
                        Number.class.isAssignableFrom(type) ||
                        Date.class.isAssignableFrom(type) ||
                        Temporal.class.isAssignableFrom(type) ||
                        URI.class == type ||
                        URL.class == type ||
                        Locale.class == type ||
                        Class.class == type));
    }

    public static boolean isCollType(Class<?> type) {
        return type.isArray() || Collection.class.isAssignableFrom(type);
    }

    public static boolean isMapType(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    public static String getJsonText(PsiParameterList psiParameterList1) {
        JsonObject jsonObject = toParamNameListNew(psiParameterList1);
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        return gson.toJson(jsonObject);
    }

    public static JsonObject toParamNameListNew(PsiParameterList parameterList) {
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String key = parameter.getName();

            PsiType type = parameter.getType();
            JsonElement jsonElement = JsonElementUtil.toJson(type, 0);
            jsonObject.add(key, jsonElement);
        }
        return jsonObject;
    }

    public static String getSimpleText(PsiParameterList parameterList) {
        JsonObject jsonObject = new JsonObject();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String key = parameter.getName();
            jsonObject.add(key, null);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        return gson.toJson(jsonObject);
    }

    /**
     * 获取JsonObject的所有key
     */
    public static Set<String> getJsonObjectKey(JsonObject jsonObject) {
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            keys.add(key);
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                keys.addAll(getJsonObjectKey(value.getAsJsonObject()));
            }
        }
        return keys;
    }

    public static boolean isJsonKey(PsiElement psiElement) {
        if (psiElement == null) {
            return false;
        }
        if (psiElement instanceof LeafPsiElement && psiElement.getParent() != null) {
            psiElement = psiElement.getParent();
        }
        PsiElement nextSibling = psiElement.getNextSibling();
        while (nextSibling instanceof PsiWhiteSpace) {
            nextSibling = nextSibling.getNextSibling();
        }
        // 当前位置后面有冒号的
        boolean isJsonKey = nextSibling != null && nextSibling.getText().equals(":");
        if (isJsonKey) {
            return true;
        }
        if (nextSibling instanceof PsiErrorElementImpl) {
            return ((PsiErrorElementImpl) nextSibling).getErrorDescription().contains("':' expected");
        }
        PsiElement psiElementParent = psiElement;
        while (psiElementParent.getParent() != null) {
            psiElementParent = psiElementParent.getParent();
        }
        if (isJsonKey(psiElementParent.getText(), psiElement.getTextOffset())) {
            return true;
        }
        return false;
    }

    public static boolean isJsonKey(String json, int index) {
        if (json == null || json.isBlank()) {
            return false;
        }
        if (index >= json.length()) {
            return false;
        }

        // 当前位置前面有,并且是在:之后的
        int colonIndex = json.lastIndexOf(':', index); // 查找当前索引之前的最后一个冒号
        int commaIndex = json.lastIndexOf(',', index); // 查找当前索引之前的最后一个冒号
        if (colonIndex < commaIndex && commaIndex < index) {
            return true;
        }
        // 当前位置的前一个非空字符是 { 123
        char preChar = '\0';
        for (int i = index - 1; i >= 0; i--) {
            char ch = json.charAt(i);
            if (!Character.isWhitespace(ch)) {
                preChar = ch; // 返回找到的非空格字符
                break;
            }
        }
        return 123 == preChar;
    }

}
