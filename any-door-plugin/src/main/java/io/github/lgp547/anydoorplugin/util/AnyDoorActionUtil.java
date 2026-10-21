package io.github.lgp547.anydoorplugin.util;

import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.TypeConversionUtil;
import io.github.lgp547.anydoorplugin.dialog.utils.IdeClassUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AnyDoorActionUtil {

    public static String genCacheKey(PsiClass psiClass, PsiMethod psiMethod) {
        return genCacheKey(psiClass.getQualifiedName(), psiMethod.getName(), AnyDoorActionUtil.toParamTypeNameList(psiMethod.getParameterList()));
    }

    public static String genCacheKey(String className, String methodName, List<String> paramTypeNameList) {
        return className + "#" + methodName + "#" + String.join(",", paramTypeNameList);
    }

    public static List<String> toParamTypeNameList(PsiParameterList parameterList) {
        List<String> list = new ArrayList<>();
        for (PsiParameter parameter : parameterList.getParameters()) {
            list.add(StringUtils.substringBefore(parameter.getType().getCanonicalText(), "<"));
        }
        return list;
    }

    /** 运行请求使用擦除后的 JVM 类型；缓存仍保留原有源码类型名称。 */
    public static List<String> toJvmParamTypeNameList(PsiParameterList parameterList) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            list.add(toJvmTypeName(TypeConversionUtil.erasure(parameter.getType())));
        }
        return list;
    }

    private static String toJvmTypeName(PsiType type) {
        if (type instanceof PsiArrayType arrayType) {
            return toJvmTypeName(arrayType.getComponentType()) + "[]";
        }
        if (type instanceof PsiClassType classType) {
            PsiClass psiClass = classType.resolve();
            if (psiClass != null) {
                return IdeClassUtil.getJvmClassName(psiClass);
            }
        }
        return type.getCanonicalText();
    }

    public static Map<String, String> getParamTypeNameTransformer(PsiParameterList parameterList) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < parameterList.getParametersCount(); i++) {
            PsiParameter parameter = Objects.requireNonNull(parameterList.getParameter(i));
            map.put(parameter.getName(), "args" + i);
        }
        return map;
    }
}
