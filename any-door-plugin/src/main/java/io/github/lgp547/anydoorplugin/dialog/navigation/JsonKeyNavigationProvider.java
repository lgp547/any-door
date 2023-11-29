package io.github.lgp547.anydoorplugin.dialog.navigation;

import com.intellij.navigation.DirectNavigationProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.dialog.JSONEditor;
import io.github.lgp547.anydoorplugin.util.JsonElementUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * 直接导航
 */
@SuppressWarnings("UnstableApiUsage")
public class JsonKeyNavigationProvider implements DirectNavigationProvider {

    @Override
    public @Nullable PsiElement getNavigationElement(@NotNull PsiElement element) {
        if (!element.getContainingFile().getName().equals(JSONEditor.ANY_DOOR_PARAM_FILE_NAME)) {
            return null;
        }

        // 非key不做处理
        if (!JsonElementUtil.isJsonKey(element)) {
            return null;
        }

        PsiParameterList psiParameterList = element.getContainingFile().getUserData(JSONEditor.ANY_DOOR_EDIT_PARAMS);
        if (psiParameterList == null) {
            return null;
        }

        // 导航到方法参数上
        String text = StringUtils.removeEnd(StringUtils.removeStart(element.getText(), "\""), "\"");
        for (int i = 0; i < psiParameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(psiParameterList.getParameter(i));
            if (Objects.equals(parameter.getName(), text)) {
                return parameter;
            }
            // 若是负责类，尝试导航到参数类里面
            if (parameter.getType() instanceof PsiClassType) {
                PsiClass psiClass = ((PsiClassType) parameter.getType()).resolve();
                if (psiClass != null) {
                    for (PsiField field : psiClass.getFields()) {
                        if (Objects.equals(field.getName(), text)) {
                            return field;
                        }
                    }
                }
            }
        }


        return null;
    }


}
