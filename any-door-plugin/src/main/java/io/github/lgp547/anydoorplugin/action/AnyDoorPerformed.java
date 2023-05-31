package io.github.lgp547.anydoorplugin.action;

import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.AnyDoorInfo;
import io.github.lgp547.anydoorplugin.dialog.TextAreaDialog;
import io.github.lgp547.anydoorplugin.dto.ParamCacheDto;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.HttpUtil;
import io.github.lgp547.anydoorplugin.util.ImportNewUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import io.github.lgp547.anydoorplugin.util.JsonUtil;
import io.github.lgp547.anydoorplugin.util.VmUtil;
import org.jetbrains.annotations.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * 打开任意门，核心执行
 */
public class AnyDoorPerformed {

    public void invoke(Project project, PsiMethod method, Runnable okAction) {
        PsiClass psiClass = (PsiClass) method.getParent();
        String className = psiClass.getQualifiedName();
        String methodName = method.getName();
        List<String> paramTypeNameList = toParamTypeNameList(method.getParameterList());


        Optional<AnyDoorSettingsState> anyDoorSettingsStateOpt = AnyDoorSettingsState.getAnyDoorSettingsStateNotExc(project);
        if (anyDoorSettingsStateOpt.isEmpty()) {
            return;
        }

        AnyDoorSettingsState service = anyDoorSettingsStateOpt.get();
        BiConsumer<String, Exception> openExcConsumer = (url, e) -> NotifierUtil.notifyError(project, "call " + url + " error [ " + e.getMessage() + " ]");


        if (paramTypeNameList.isEmpty()) {
            String jsonDtoStr = getJsonDtoStr(className, methodName, paramTypeNameList, "{}", !service.enableAsyncExecute, null);
            openAnyDoor(project, jsonDtoStr, service, openExcConsumer);
        } else {
            String cacheKey = getCacheKey(className, methodName, paramTypeNameList);

            TextAreaDialog dialog = new TextAreaDialog(project, String.format("fill method(%s) param", methodName), method.getParameterList(), service.getCache(cacheKey), service);
            dialog.setOkAction(() -> {
                okAction.run();
                if (dialog.isChangePid()) {
                    service.pid = dialog.getPid();
                }
                String text = dialog.getText();
                ParamCacheDto paramCacheDto = new ParamCacheDto(dialog.getRunNum(), dialog.getIsConcurrent(), text);
                service.putCache(cacheKey, paramCacheDto);
                // Change to args for the interface type
                if (psiClass.isInterface()) {
                    text = JsonUtil.transformedKey(text, getParamTypeNameTransformer(method.getParameterList()));
                }
                text = JsonUtil.compressJson(text);
                String jsonDtoStr = getJsonDtoStr(className, methodName, paramTypeNameList, text, !service.enableAsyncExecute, paramCacheDto);
                openAnyDoor(project, jsonDtoStr, service, openExcConsumer);
            });
            dialog.show();
        }
    }

    private static void openAnyDoor(Project project, String jsonDtoStr, AnyDoorSettingsState service, BiConsumer<String, Exception> errHandle) {
        if (service.isSelectJavaAttach()) {
            String anyDoorJarPath = ImportNewUtil.getPluginLibPath(AnyDoorInfo.ANY_DOOR_JAR, service.dependenceVersion);
            String paramPath = project.getBasePath() + "/.idea/AnyDoorParam.json";
            VmUtil.attachAsync(String.valueOf(service.pid), anyDoorJarPath, jsonDtoStr, paramPath, errHandle);
        } else {
            HttpUtil.postAsyncByJdk(service.mvcAddress + ":" + service.mvcPort + service.mvcWebPathPrefix + "/any_door/run", jsonDtoStr, errHandle);
        }
    }

    @NotNull
    private static String getJsonDtoStr(String className, String methodName, List<String> paramTypeNameList, String content, boolean isSync, @Nullable ParamCacheDto paramCacheDto) {
        JsonObject jsonObjectReq = new JsonObject();
        jsonObjectReq.addProperty("content", content);
        jsonObjectReq.addProperty("methodName", methodName);
        jsonObjectReq.addProperty("className", className);
        jsonObjectReq.addProperty("sync", isSync);
        if (paramCacheDto != null) {
            jsonObjectReq.addProperty("num", paramCacheDto.getRunNum());
            jsonObjectReq.addProperty("concurrent", paramCacheDto.getConcurrent());
        }
        jsonObjectReq.add("parameterTypes", JsonUtil.toJsonArray(paramTypeNameList));
        return jsonObjectReq.toString();
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

    private static Map<String, String> getParamTypeNameTransformer(PsiParameterList parameterList) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            map.put(parameter.getName(), "args" + i);
        }
        return map;
    }

}