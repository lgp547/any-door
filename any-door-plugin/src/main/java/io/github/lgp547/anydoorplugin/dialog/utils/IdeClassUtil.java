package io.github.lgp547.anydoorplugin.dialog.utils;

import java.util.Objects;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-26 19:03
 **/
public class IdeClassUtil {

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
        return methodName.substring(0, methodName.indexOf("("));
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
