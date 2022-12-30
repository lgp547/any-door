package io.github.lgp547.anydoorplugin.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.HttpUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public class AnyDoorPerformed {

    public void invoke(Project project, PsiMethod method) {
        String className = ((PsiClass) method.getParent()).getQualifiedName();
        String methodName = method.getName();
        List<String> paramTypeNameList = toParamTypeNameList(method.getParameterList());

        Optional<AnyDoorSettingsState> anyDoorSettingsStateOp = getAnyDoorSettingsState(project);
        if (anyDoorSettingsStateOp.isEmpty()) {
            return;
        }

        AnyDoorSettingsState service = anyDoorSettingsStateOp.get();
        Integer port = service.port;
        BiConsumer<String, Exception> openExcConsumer = (url, e) -> NotifierUtil.notifyError(project, "call " + url + " error [ " + e.getMessage() + " ]");


        if (paramTypeNameList.isEmpty()) {
            openAnyDoor(className, methodName, paramTypeNameList, "{}", port, openExcConsumer);
        } else {

            String initContent;
            String cacheKey = getCacheKey(className, methodName, paramTypeNameList);
            String cache = service.getCache(cacheKey);
            if (cache != null) {
                initContent = cache;
            } else {
                // generate default json
                List<String> parameterNames = toParamNameList(method.getParameterList());
                JsonObject jsonObject = new JsonObject();
                parameterNames.forEach(name -> jsonObject.add(name, JsonNull.INSTANCE));
                Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
                initContent = gson.toJson(jsonObject);
            }

            TextAreaDialog dialog = new TextAreaDialog(project, "Generate call code", initContent);
            dialog.setOkAction(() -> {
                service.putCache(cacheKey, dialog.getText());
                openAnyDoor(className, methodName, paramTypeNameList, dialog.getText(), port, openExcConsumer);
            });
            dialog.show();
        }
    }

    /**
     * prevention error
     */
    private Optional<AnyDoorSettingsState> getAnyDoorSettingsState(@NotNull Project project) {
        try {
            return Optional.ofNullable(project.getService(AnyDoorSettingsState.class));
        } catch (Exception e) {
            NotifierUtil.notifyError(project, "get AnyDoorSettings Service error. errMsg:" + e.getMessage());
            return Optional.empty();
        }
    }

    private static void openAnyDoor(String className, String methodName,
                                    List<String> paramTypeNameList, String content, Integer port, BiConsumer<String, Exception> errHandle) {
        JsonObject jsonObjectReq = new JsonObject();
        jsonObjectReq.addProperty("content", content);
        jsonObjectReq.addProperty("methodName", methodName);
        jsonObjectReq.addProperty("className", className);
        jsonObjectReq.add("parameterTypes", toJsonArray(paramTypeNameList));

        HttpUtil.postAsyncByJdk("http://127.0.0.1:" + port + "/any_door/run", jsonObjectReq.toString(), errHandle);
    }

    private static String getCacheKey(String className, String methodName, List<String> paramTypeNameList) {
        return className + "#" + methodName + "#" + String.join(",", paramTypeNameList);
    }

    private static List<String> toParamTypeNameList(PsiParameterList parameterList) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String canonicalText = parameter.getType().getCanonicalText();
            list.add(canonicalText);
        }
        return list;
    }

    private static List<String> toParamNameList(PsiParameterList parameterList) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            list.add(parameter.getName());
        }
        return list;
    }

    private static JsonArray toJsonArray(List<String> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

}