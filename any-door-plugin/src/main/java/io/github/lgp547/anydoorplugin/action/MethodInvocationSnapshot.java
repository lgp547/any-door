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
        List<String> parameterTypeNames,
        Map<String, String> parameterNameTransformer,
        boolean interfaceType
) {

    MethodInvocationSnapshot {
        parameterTypeNames = List.copyOf(parameterTypeNames);
        parameterNameTransformer = Map.copyOf(parameterNameTransformer);
    }

    static MethodInvocationSnapshot capture(PsiMethod psiMethod) {
        return ReadAction.compute(() -> captureInsideReadAction(
                (PsiClass) psiMethod.getParent(),
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
        return new MethodInvocationSnapshot(
                psiClass.getQualifiedName(),
                psiMethod.getName(),
                IdeClassUtil.getMethodQualifiedName(psiMethod),
                AnyDoorActionUtil.toParamTypeNameList(parameterList),
                AnyDoorActionUtil.getParamTypeNameTransformer(parameterList),
                psiClass.isInterface()
        );
    }
}
