package io.github.lgp547.anydoorplugin.action;

import com.google.gson.*;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.jvm.JvmNamedElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.HttpUtil;
import io.github.lgp547.anydoorplugin.util.PsiUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@NonNls
public class AnyDoorIntention extends PsiElementBaseIntentionAction implements IntentionAction {

    @NotNull
    public String getText() {
        return "Open any door";
    }

    @NotNull
    public String getFamilyName() {
        return "Any door";
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
        if (element == null) {
            return false;
        }

        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        return method != null && !PsiUtils.isStatic(method);
    }

    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
            throws IncorrectOperationException {
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        assert method != null;
        PsiClass psiClass = PsiUtils.getDeclaringClass(method);

        List<String> parameterNames = Arrays.stream(method.getParameters())
                .map(JvmNamedElement::getName)
                .collect(Collectors.toList());

        // 生成默认的 json
        JsonObject jsonObject = new JsonObject();
        parameterNames.forEach(name -> jsonObject.add(name, JsonNull.INSTANCE));
        Gson gson1 = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .create();
        String placeHolder = gson1.toJson(jsonObject);

        TextAreaDialog dialog = new TextAreaDialog(project, "Generate call code", placeHolder);
        dialog.setOkAction(() -> {
            String content = dialog.getText();
            String className = psiClass.getQualifiedName();
            String methodName = method.getName();

            JsonArray parameterTypes = new JsonArray();
            PsiParameterList parameterList = method.getParameterList();
            for (int i = 0; i < parameterList.getParametersCount(); i++) {
                PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
                String canonicalText = parameter.getType().getCanonicalText();
                parameterTypes.add(canonicalText);
            }
            Integer port = project.getService(AnyDoorSettingsState.class).port;

            JsonObject jsonObjectReq = new JsonObject();
            jsonObjectReq.addProperty("content", content);
            jsonObjectReq.addProperty("methodName", methodName);
            jsonObjectReq.addProperty("className", className);
            jsonObjectReq.add("parameterTypes", parameterTypes);

            // http 请求
            Thread thread = new Thread(() -> HttpUtil.post("http://127.0.0.1:" + port + "/any_door/run", jsonObjectReq.toString()));
            thread.start();


        });
        dialog.show();
    }

    public boolean startInWriteAction() {
        return false;
    }



}