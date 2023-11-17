package io.github.lgp547.anydoorplugin.dialog.completion;

import com.intellij.lang.injection.general.Injection;
import com.intellij.lang.injection.general.LanguageInjectionContributor;
import com.intellij.lang.injection.general.SimpleInjection;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import io.github.lgp547.anydoorplugin.AnyDoorInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 贡献者
 * lgp todo: 需要类型以及包名
 */
public final class JsonInjectorContributor implements LanguageInjectionContributor {


    @Override
    public @Nullable Injection getInjection(@NotNull PsiElement context) {
        if (isInjectable(context)) {
            Project project = context.getProject();
            return new SimpleInjection(JavaLanguage.INSTANCE, "import test2.run.*; public class Test { String u = ", ";}", null);
        }
        return null;
    }

    public static boolean isInjectable(PsiElement context) {
        if (context instanceof PsiLanguageInjectionHost) {
            String text = context.getText();
            boolean b = text != null && text.startsWith("\"`") && text.endsWith("`\"") && text.length() > 4;
            boolean equals = context.getContainingFile().getName().equals(AnyDoorInfo.ANY_DOOR_PARAM_FILE_NAME);
            return b && equals;
        }
        return false;
    }

    @NotNull
    public static TextRange getRangeInsideHost(@NotNull PsiElement context) {
        return new TextRange(2, context.getTextLength() - 2);
    }


}