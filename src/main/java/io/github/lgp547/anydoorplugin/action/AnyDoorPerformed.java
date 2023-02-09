package io.github.lgp547.anydoorplugin.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.HttpUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * 打开任意门，核心执行
 */
public class AnyDoorPerformed {

    public void invoke(Project project, PsiMethod method) {
        String className = ((PsiClass) method.getParent()).getQualifiedName();
        String methodName = method.getName();
        List<String> paramTypeNameList = toParamTypeNameList(method.getParameterList());

        Optional<AnyDoorSettingsState> anyDoorSettingsStateOpt = AnyDoorSettingsState.getAnyDoorSettingsStateNotExc(project);
        if (anyDoorSettingsStateOpt.isEmpty()) {
            return;
        }

        AnyDoorSettingsState service = anyDoorSettingsStateOpt.get();
        BiConsumer<String, Exception> openExcConsumer = (url, e) -> NotifierUtil.notifyError(project, "call " + url + " error [ " + e.getMessage() + " ]");


        if (paramTypeNameList.isEmpty()) {
            openAnyDoor(className, methodName, paramTypeNameList, "{}", service, openExcConsumer);
        } else {
            String cacheKey = getCacheKey(className, methodName, paramTypeNameList);
            String cacheContent = service.getCache(cacheKey);

            TextAreaDialog dialog = new TextAreaDialog(project, "fill call param", method.getParameterList(), cacheContent);
            dialog.setOkAction(() -> {
                service.putCache(cacheKey, dialog.getText());
                openAnyDoor(className, methodName, paramTypeNameList, dialog.getText(), service, openExcConsumer);
            });
            dialog.show();
        }
    }

    private static void openAnyDoor(String className, String methodName,
                                    List<String> paramTypeNameList, String content, AnyDoorSettingsState service, BiConsumer<String, Exception> errHandle) {
        JsonObject jsonObjectReq = new JsonObject();
        jsonObjectReq.addProperty("content", content);
        jsonObjectReq.addProperty("methodName", methodName);
        jsonObjectReq.addProperty("className", className);
        jsonObjectReq.add("parameterTypes", toJsonArray(paramTypeNameList));

        HttpUtil.postAsyncByJdk("http://127.0.0.1:" + service.port + service.webPathPrefix + "/any_door/run", jsonObjectReq.toString(), errHandle);
    }

    private static String getCacheKey(String className, String methodName, List<String> paramTypeNameList) {
        return className + "#" + methodName + "#" + String.join(",", paramTypeNameList);
    }

    private static List<String> toParamTypeNameList(PsiParameterList parameterList) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            String canonicalText = parameter.getType().getCanonicalText();
            list.add(StringUtils.substringBefore(canonicalText, "<"));
        }
        return list;
    }

    private static JsonArray toJsonArray(List<String> list) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(jsonArray::add);
        return jsonArray;
    }

}