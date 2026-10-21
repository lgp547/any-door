package io.github.lgp547.anydoorplugin.dialog.utils;

import java.util.Objects;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.ULambdaExpression;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-26 19:03
 **/
public class IdeClassUtil {

    /**
     * 从源码位置取得 JVM 方法；Kotlin 使用 light method，共用后续 Java PSI 流程。
     * 局部函数和构造器没有现有协议可直接调用的普通方法入口。
     */
    @Nullable
    public static PsiMethod findMethod(PsiElement element) {
        for (PsiElement current = element; current != null; current = current.getParent()) {
            if (current instanceof PsiMethod method) {
                return method.isConstructor() ? null : method;
            }
            if (!"kotlin".equalsIgnoreCase(current.getLanguage().getID())) {
                continue;
            }
            UElement declaration = UastContextKt.toUElement(current);
            // 局部命名函数也映射为 UAST lambda；普通 lambda 内的光标继续定位所属方法。
            if (declaration instanceof ULambdaExpression lambda
                    && lambda.getSourcePsi() instanceof PsiNameIdentifierOwner) {
                return null;
            }
            if (declaration instanceof UMethod method) {
                return method.isConstructor() || !(method.getUastParent() instanceof UClass)
                        ? null : method.getJavaPsi();
            }
        }
        return null;
    }

    /** 识别 light method 对应的源码语言，避免依赖某个 Kotlin 编译器的 PSI 实现类。 */
    public static boolean isKotlinMethod(PsiMethod method) {
        return "kotlin".equalsIgnoreCase(method.getNavigationElement().getLanguage().getID());
    }

    /** Class.forName 使用二进制类名，嵌套类不能沿用源码中的点号。 */
    public static String getJvmClassName(PsiClass psiClass) {
        PsiClass containingClass = psiClass.getContainingClass();
        if (containingClass != null) {
            return getJvmClassName(containingClass) + "$" + psiClass.getName();
        }
        return Objects.requireNonNull(psiClass.getQualifiedName(), "JVM class name is required");
    }

    public static String getMethodQualifiedName(PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass != null) {
            StringBuilder fullQualifiedName = new StringBuilder(containingClass.getQualifiedName() + "#" + psiMethod.getName());
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            if (parameters.length > 0) {
                fullQualifiedName.append("(");
                for (int i = 0; i < parameters.length; i++) {
                    fullQualifiedName.append(parameters[i].getType().getCanonicalText());
                    if (i < parameters.length - 1) {
                        fullQualifiedName.append(",");
                    }
                }
                fullQualifiedName.append(")");
            }
            return fullQualifiedName.toString();
        } else {
            return psiMethod.getName();
        }
    }

    public static String getSimpleMethodName(String qualifiedMethodName) {
        String methodName = qualifiedMethodName.substring(qualifiedMethodName.lastIndexOf("#") + 1);
        int parameterStart = methodName.indexOf("(");
        return parameterStart < 0 ? methodName : methodName.substring(0, parameterStart);
    }

    @Nullable
    public static PsiClass findClass(Project project, String qualifiedClassName) {
        return JavaPsiFacade.getInstance(project).findClass(qualifiedClassName, GlobalSearchScope.allScope(project));
    }

    public static PsiMethod findMethod(Project project, String qualifiedMethodName) {
        PsiClass psiClass = findClass(project, qualifiedMethodName.substring(0, qualifiedMethodName.lastIndexOf("#")));
        if (Objects.nonNull(psiClass)) {
            PsiMethod[] methods = psiClass.findMethodsByName(getSimpleMethodName(qualifiedMethodName), false);
            for (PsiMethod method : methods) {
                if (Objects.equals(getMethodQualifiedName(method), qualifiedMethodName)) {
                    return method;
                }
            }
        }
        return null;
    }
}
