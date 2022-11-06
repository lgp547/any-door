package io.github.lgp547.anydoorplugin.util;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayQuery;
import com.intellij.util.EmptyQuery;
import com.intellij.util.MergeQuery;
import com.intellij.util.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class PsiUtils {
    public static Optional<PsiClass> findClassByName(Project project, String name) {
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        return Optional.ofNullable(javaPsiFacade.findClass(name, scope));
    }

    public static boolean hasAnnotation(PsiMethod method, String annotationQualifiedName) {
        return Arrays.stream(method.getModifierList().getAnnotations())
                .anyMatch(psiAnnotation ->
                        psiAnnotation.hasQualifiedName(annotationQualifiedName));
    }

    public static boolean isStatic(PsiMethod method) {
        return method.hasModifier(JvmModifier.STATIC);
    }

    public static PsiClass getDeclaringClass(PsiMethod method) {
        return (PsiClass) method.getParent();
    }

    public static Optional<List<PsiMethod>> findMethodsByName(Project project, String className, String methodName) {
        return findClassByName(project, className)
                .map(c -> Arrays.asList(c.findMethodsByName(methodName, false)));
    }

    /**
     * @return psiElement 作为参数出现的所有方法
     */
    public static Query<PsiMethod> findParameterUsages(PsiElement psiElement) {
        return ReferencesSearch.search(psiElement)
                .mapping(ref -> {
                    PsiElement element = ref.getElement();
                    return (PsiMethod) PsiTreeUtil.findFirstParent(element, e -> e instanceof PsiMethod);
                })
                .filtering(Objects::nonNull);
    }

    public static Query<PsiMethod> findCaller(PsiMethod method) {
        return ReferencesSearch.search(method)
                .filtering(r -> {
                    PsiElement element = r.getElement();
                    return element.getParent() instanceof PsiMethodCallExpression;
                })
                .mapping(r -> {
                    PsiMethodCallExpression callee = (PsiMethodCallExpression) r.getElement().getParent();
                    return (PsiMethod) PsiTreeUtil.findFirstParent(callee, e -> e instanceof PsiMethod);
                });
    }

    public static Query<PsiMethod> findCallerRecur(PsiMethod method, int n) {
        if (n == 0) {
            return new EmptyQuery<>();
        }

        return merge(findCaller(method).findAll()
                .stream()
                .map(caller -> new MergeQuery<>(unitQuery(caller), findCallerRecur(caller, n - 1)))
                .collect(Collectors.toList()));
    }

    private static Query<PsiMethod> merge(List<Query<PsiMethod>> qs) {
        if (qs.isEmpty()) {
            return new EmptyQuery<>();
        } else {
            Query<PsiMethod> q1 = qs.get(0);
            Query<PsiMethod> rest = merge(qs.subList(1, qs.size()));
            return new MergeQuery<>(q1, rest);
        }
    }

    private static Query<PsiMethod> unitQuery(PsiMethod a) {
        return new ArrayQuery<>(a);
    }

}
