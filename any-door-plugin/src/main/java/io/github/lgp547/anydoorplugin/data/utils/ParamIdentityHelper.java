package io.github.lgp547.anydoorplugin.data.utils;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-17 11:22
 **/
public class ParamIdentityHelper {

    public static String getMethodQualifiedName(PsiMethod psiMethod) {
        PsiClass containingClass = psiMethod.getContainingClass();
        if (containingClass != null) {
            String fullQualifiedName = containingClass.getQualifiedName() + "#" + psiMethod.getName();
            PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
            if (parameters.length > 0) {
                fullQualifiedName += "(";
                for (int i = 0; i < parameters.length; i++) {
                    fullQualifiedName += parameters[i].getType().getCanonicalText();
                    if (i < parameters.length - 1) {
                        fullQualifiedName += ",";
                    }
                }
                fullQualifiedName += ")";
            }
            return fullQualifiedName;
        } else {
            return psiMethod.getName();
        }
    }

    public static String getSimpleMethodName(String qualifiedMethodName) {
        String methodName = qualifiedMethodName.substring(qualifiedMethodName.lastIndexOf("#") + 1);
        return methodName.substring(0, methodName.indexOf("("));
    }

}
