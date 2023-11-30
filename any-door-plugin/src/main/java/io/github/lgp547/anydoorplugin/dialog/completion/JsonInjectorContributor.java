//package io.github.lgp547.anydoorplugin.dialog.completion;
//
//import com.intellij.lang.injection.general.Injection;
//import com.intellij.lang.injection.general.LanguageInjectionContributor;
//import com.intellij.lang.injection.general.SimpleInjection;
//import com.intellij.lang.java.JavaLanguage;
//import com.intellij.openapi.util.TextRange;
//import com.intellij.psi.PsiElement;
//import com.intellij.psi.PsiLanguageInjectionHost;
//import com.intellij.psi.PsiParameterList;
//import com.intellij.psi.impl.source.PsiJavaFileBaseImpl;
//import io.github.lgp547.anydoorplugin.dialog.JSONEditor;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.Optional;
//
///**
// * 贡献者
// */
//public final class JsonInjectorContributor implements LanguageInjectionContributor {
//
//    @Override
//    public @Nullable Injection getInjection(@NotNull PsiElement context) {
//        if (isInjectable(context)) {
//            PsiParameterList psiParameterList = context.getContainingFile().getUserData(JSONEditor.ANY_DOOR_EDIT_PARAMS);
//            String projectPackName = Optional.ofNullable(psiParameterList).map(PsiElement::getParent).map(PsiElement::getParent).map(PsiElement::getParent).map(item -> {
//                if (item instanceof PsiJavaFileBaseImpl) {
//                    return ((PsiJavaFileBaseImpl) item).getPackageName() + ".*";
//                }
//                return "";
//            }).orElse("");
//
//            String typeName = "String";
//
//            String prefix = String.format("import %s; public class AnyDoorInjectionClass { %s u = ", projectPackName, typeName);
//            String suffix = ";}";
//            return new SimpleInjection(JavaLanguage.INSTANCE, prefix, suffix, null);
//        }
//        return null;
//    }
//
//    public static boolean isInjectable(PsiElement context) {
//        if (context instanceof PsiLanguageInjectionHost) {
//            String text = context.getText();
//            boolean b = text != null && text.startsWith("\"`") && text.endsWith("`\"") && text.length() > 4;
//            boolean equals = context.getContainingFile().getName().equals(JSONEditor.ANY_DOOR_PARAM_FILE_NAME);
//            return b && equals;
//        }
//        return false;
//    }
//
//    @NotNull
//    public static TextRange getRangeInsideHost(@NotNull PsiElement context) {
//        return new TextRange(2, context.getTextLength() - 2);
//    }
//
//
//}