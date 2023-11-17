package io.github.lgp547.anydoorplugin.dialog.completion;

import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.injection.general.Injection;
import com.intellij.lang.injection.general.LanguageInjectionPerformer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class JsonInjectionPerformer implements LanguageInjectionPerformer {

    @Override
    public boolean isPrimary() {
        return true;
    }

    @Override
    public boolean performInjection(@NotNull MultiHostRegistrar registrar, @NotNull Injection injection, @NotNull PsiElement context) {
        if (!JsonInjectorContributor.isInjectable(context)) {
            return false;
        }
        Language injectedLanguage = injection.getInjectedLanguage();
        registrar.startInjecting(Objects.requireNonNull(injectedLanguage));
        registrar.addPlace(injection.getPrefix(), injection.getSuffix(), (PsiLanguageInjectionHost) context, JsonInjectorContributor.getRangeInsideHost(context));
        registrar.doneInjecting();
        return true;
    }


}
