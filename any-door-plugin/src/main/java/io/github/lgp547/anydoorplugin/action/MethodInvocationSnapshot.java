package io.github.lgp547.anydoorplugin.action;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.dialog.utils.IdeClassUtil;
import io.github.lgp547.anydoorplugin.util.AnyDoorActionUtil;

import java.util.List;
import java.util.Map;

record MethodInvocationSnapshot(
        String className,
        String methodName,
        String qualifiedMethodName,
        String cacheKey,
        List<String> parameterTypeNames,
        Map<String, String> parameterNameTransformer,
        boolean positionalParameters
) {

    MethodInvocationSnapshot {
        parameterTypeNames = List.copyOf(parameterTypeNames);
        parameterNameTransformer = Map.copyOf(parameterNameTransformer);
    }

    static MethodInvocationSnapshot capture(PsiMethod psiMethod) {
        return ReadAction.compute(() -> captureInsideReadAction(
                psiMethod.getContainingClass(),
                psiMethod
        ));
    }

    static MethodInvocationSnapshot capture(PsiClass psiClass, PsiMethod psiMethod) {
        return ReadAction.compute(() -> captureInsideReadAction(psiClass, psiMethod));
    }

    private static MethodInvocationSnapshot captureInsideReadAction(
            PsiClass psiClass,
            PsiMethod psiMethod
    ) {
        PsiParameterList parameterList = psiMethod.getParameterList();
        List<String> parameterTypes = AnyDoorActionUtil.toJvmParamTypeNameList(parameterList);
        boolean kotlinMethod = IdeClassUtil.isKotlinMethod(psiMethod);
        if (kotlinMethod && parameterTypes.contains("kotlin.coroutines.Continuation")) {
            throw new UnsupportedSuspendMethodException();
        }
        return new MethodInvocationSnapshot(
                IdeClassUtil.getJvmClassName(psiClass),
                psiMethod.getName(),
                IdeClassUtil.getMethodQualifiedName(psiMethod),
                AnyDoorActionUtil.genCacheKey(psiClass, psiMethod),
                parameterTypes,
                AnyDoorActionUtil.getParamTypeNameTransformer(parameterList),
                psiClass.isInterface() || kotlinMethod
        );
    }

    /** 参数历史仍使用源码类名，运行请求单独使用二进制类名。 */
    String sourceClassName() {
        return qualifiedMethodName.substring(0, qualifiedMethodName.indexOf('#'));
    }
}
