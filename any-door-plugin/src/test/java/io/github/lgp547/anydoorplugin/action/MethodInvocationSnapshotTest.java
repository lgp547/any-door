package io.github.lgp547.anydoorplugin.action;

import com.intellij.mock.MockApplication;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import junit.framework.TestCase;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

public class MethodInvocationSnapshotTest extends TestCase {

    private Disposable disposable;
    private TrackingMockApplication application;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        disposable = Disposer.newDisposable();
        application = new TrackingMockApplication(disposable);
        ApplicationManager.setApplication(application, disposable);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            Disposer.dispose(disposable);
        } finally {
            super.tearDown();
        }
    }

    public void testCaptureCopiesInterfaceMethodMetadata() {
        PsiParameter name = parameter("name", new TestPsiType("java.lang.String"));
        PsiParameter ids = parameter("ids", new TestPsiType("java.util.List<java.lang.Integer>"));
        PsiParameterList parameters = parameterList(name, ids);
        PsiClass psiClass = psiClass("demo.SampleService", true);
        PsiMethod psiMethod = psiMethod("execute", psiClass, parameters);

        MethodInvocationSnapshot snapshot = MethodInvocationSnapshot.capture(psiMethod);

        assertTrue(application.wasReadActionInvoked());
        assertEquals("demo.SampleService", snapshot.className());
        assertEquals("execute", snapshot.methodName());
        assertEquals(
                "demo.SampleService#execute(java.lang.String,java.util.List<java.lang.Integer>)",
                snapshot.qualifiedMethodName()
        );
        assertEquals(
                List.of("java.lang.String", "java.util.List"),
                snapshot.parameterTypeNames()
        );
        assertEquals(
                Map.of("name", "args0", "ids", "args1"),
                snapshot.parameterNameTransformer()
        );
        assertTrue(snapshot.interfaceType());
    }

    public void testCaptureMarksClassMethodAsNonInterface() {
        PsiParameterList parameters = parameterList(
                parameter("count", new TestPsiType("int"))
        );
        PsiClass psiClass = psiClass("demo.SampleService", false);
        PsiMethod psiMethod = psiMethod("execute", psiClass, parameters);

        MethodInvocationSnapshot snapshot = MethodInvocationSnapshot.capture(psiMethod);

        assertTrue(application.wasReadActionInvoked());
        assertFalse(snapshot.interfaceType());
        assertEquals(List.of("int"), snapshot.parameterTypeNames());
        assertEquals(Map.of("count", "args0"), snapshot.parameterNameTransformer());
    }

    private static PsiClass psiClass(String qualifiedName, boolean interfaceType) {
        return proxy(PsiClass.class, (proxy, method, args) -> switch (method.getName()) {
            case "getQualifiedName" -> qualifiedName;
            case "isInterface" -> interfaceType;
            case "toString" -> qualifiedName;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw unsupported(method.getName());
        });
    }

    private static PsiMethod psiMethod(
            String name,
            PsiClass containingClass,
            PsiParameterList parameters
    ) {
        return proxy(PsiMethod.class, (proxy, method, args) -> switch (method.getName()) {
            case "getName" -> name;
            case "getContainingClass" -> containingClass;
            case "getParent" -> containingClass;
            case "getParameterList" -> parameters;
            case "toString" -> name;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw unsupported(method.getName());
        });
    }

    private static PsiParameterList parameterList(PsiParameter... parameters) {
        return proxy(PsiParameterList.class, (proxy, method, args) -> switch (method.getName()) {
            case "getParametersCount" -> parameters.length;
            case "getParameter" -> parameters[(Integer) args[0]];
            case "getParameters" -> parameters;
            case "toString" -> List.of(parameters).toString();
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw unsupported(method.getName());
        });
    }

    private static PsiParameter parameter(String name, PsiType type) {
        return proxy(PsiParameter.class, (proxy, method, args) -> switch (method.getName()) {
            case "getName" -> name;
            case "getType" -> type;
            case "toString" -> name;
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw unsupported(method.getName());
        });
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                handler
        ));
    }

    private static UnsupportedOperationException unsupported(String methodName) {
        return new UnsupportedOperationException("Unexpected PSI call: " + methodName);
    }

    private static final class TestPsiType extends PsiType {

        private final String canonicalText;

        private TestPsiType(String canonicalText) {
            super(PsiAnnotation.EMPTY_ARRAY);
            this.canonicalText = canonicalText;
        }

        @Override
        public String getPresentableText() {
            return canonicalText;
        }

        @Override
        public String getCanonicalText() {
            return canonicalText;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public boolean equalsToText(String text) {
            return canonicalText.equals(text);
        }

        @Override
        public <A> A accept(PsiTypeVisitor<A> visitor) {
            return visitor.visitType(this);
        }

        @Override
        public GlobalSearchScope getResolveScope() {
            return GlobalSearchScope.EMPTY_SCOPE;
        }

        @Override
        public PsiType[] getSuperTypes() {
            return PsiType.EMPTY_ARRAY;
        }
    }

    private static final class TrackingMockApplication extends MockApplication {

        private boolean readActionInvoked;

        private TrackingMockApplication(Disposable parentDisposable) {
            super(parentDisposable);
        }

        @Override
        public <T, E extends Throwable> T runReadAction(
                ThrowableComputable<T, E> computation
        ) throws E {
            readActionInvoked = true;
            return super.runReadAction(computation);
        }

        private boolean wasReadActionInvoked() {
            return readActionInvoked;
        }
    }
}
