package io.github.lgp547.anydoorplugin.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.PsiTestUtil;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import com.intellij.util.PathUtil;
import io.github.lgp547.anydoorplugin.dialog.utils.IdeClassUtil;
import io.github.lgp547.anydoorplugin.dialog.JSONEditor;
import io.github.lgp547.anydoorplugin.dialog.navigation.JsonKeyNavigationProvider;
import io.github.lgp547.anydoorplugin.AnyDoorBundle;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.AnyDoorActionUtil;
import io.github.lgp547.anydoorplugin.util.JsonElementUtil;
import io.github.lgp547.anydoorplugin.util.JsonUtil;
import org.jetbrains.uast.UMethod;
import org.jetbrains.uast.UastContextKt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用真实 Kotlin PSI 验证方法入口、JVM 签名和参数界面的共享数据。
 *
 * @author luoweiyu
 * @date 2026-10-20
 */
public class KotlinMethodInvocationTest extends LightJavaCodeInsightFixtureTestCase {

    private static final LightProjectDescriptor PROJECT = new LightProjectDescriptor() {
        @Override
        public Sdk getSdk() {
            return JavaSdk.getInstance().createJdk("AnyDoor 测试 JDK", System.getProperty("java.home"), false);
        }
    };

    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return PROJECT;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File stdlib = new File(PathUtil.getJarPathForClass(kotlin.Unit.class));
        PsiTestUtil.addLibrary(getModule(), "kotlin-stdlib", stdlib.getParent(), stdlib.getName());
    }

    public void testKotlinMethodEnablesAction() {
        myFixture.configureByText("Service.kt", """
                package sample
                class Service {
                    fun <caret>run(count: Int, label: String?) = label ?: count.toString()
                }
                """);
        AnActionEvent event = actionEvent();
        new AnyDoorOpenAction().update(event);
        assertTrue("Kotlin 方法应显示 Open AnyDoor", event.getPresentation().isEnabledAndVisible());
    }

    public void testKotlinSnapshotAndCachedMethodUseJvmSignature() {
        myFixture.configureByText("Service.kt", """
                package sample
                class Service {
                    private fun <caret>run(count: Int, optional: Int?, values: Array<String>) = count
                }
                """);
        PsiMethod method = lightMethodAtCaret();
        MethodInvocationSnapshot snapshot = MethodInvocationSnapshot.capture(method);
        assertEquals("sample.Service", snapshot.className());
        assertEquals(List.of("int", "java.lang.Integer", "java.lang.String[]"), snapshot.parameterTypeNames());
        assertNotNull("缓存的方法签名必须能重新定位", IdeClassUtil.findMethod(getProject(), snapshot.qualifiedMethodName()));
        assertEquals("{\"count\":0,\"optional\":0,\"values\":[\"\"]}",
                JsonElementUtil.toParamNameListNew(method.getParameterList()).toString());
    }

    public void testNestedParameterAndCompanionUseBinaryClassNames() {
        myFixture.configureByText("Holder.kt", """
                package sample
                class Holder {
                    class Request(val value: String)
                    companion object {
                        fun <caret>run(request: Request) = request.value
                    }
                }
                """);
        PsiMethod method = lightMethodAtCaret();
        MethodInvocationSnapshot snapshot = MethodInvocationSnapshot.capture(method);
        assertEquals("sample.Holder$Companion", snapshot.className());
        assertEquals(List.of("sample.Holder$Request"), AnyDoorActionUtil.toJvmParamTypeNameList(method.getParameterList()));
        assertEquals("sample.Holder.Companion#run#sample.Holder.Request", snapshot.cacheKey());
        assertNotNull(IdeClassUtil.findMethod(getProject(), snapshot.qualifiedMethodName()));
    }

    public void testJavaActionStillWorks() {
        myFixture.configureByText("Service.java", """
                package sample;
                class Service {
                    private String <caret>run(int count) { return String.valueOf(count); }
                }
                """);
        AnActionEvent event = actionEvent();
        new AnyDoorOpenAction().update(event);
        assertTrue(event.getPresentation().isEnabledAndVisible());
    }

    public void testTopLevelJvmNameAndDefaultParameters() {
        myFixture.configureByText("Functions.kt", """
                @file:JvmName("PublicFunctions")
                package sample
                @JvmName("invokeRenamed")
                fun <caret>run(label: String = "default") = label
                """);
        PsiMethod method = IdeClassUtil.findMethod(myFixture.getFile().findElementAt(myFixture.getCaretOffset()));
        assertNotNull(method);
        MethodInvocationSnapshot snapshot = MethodInvocationSnapshot.capture(method);
        assertEquals("sample.PublicFunctions", snapshot.className());
        assertEquals("invokeRenamed", snapshot.methodName());
        assertEquals(List.of("java.lang.String"), snapshot.parameterTypeNames());
        assertTrue(snapshot.positionalParameters());
        assertEquals("{\"args0\":\"explicit\"}",
                JsonUtil.transformedKey("{\"label\":\"explicit\"}", snapshot.parameterNameTransformer()));
        assertNotNull(IdeClassUtil.findMethod(getProject(), snapshot.qualifiedMethodName()));
    }

    public void testObjectMethodAndBatchParameterTransformation() {
        myFixture.configureByText("Singleton.kt", """
                package sample
                object Singleton {
                    fun <caret>run(args1: String, args0: String) = args1 + args0
                }
                """);
        MethodInvocationSnapshot snapshot = MethodInvocationSnapshot.capture(lightMethodAtCaret());
        assertEquals("sample.Singleton", snapshot.className());
        assertEquals(List.of("java.lang.String", "java.lang.String"), snapshot.parameterTypeNames());
        String transformed = JsonUtil.transformedKey("[{\"args1\":\"first\",\"args0\":\"second\"}]",
                snapshot.parameterNameTransformer());
        assertTrue(transformed.contains("\"args0\":\"first\""));
        assertTrue(transformed.contains("\"args1\":\"second\""));
    }

    public void testSuspendIsRejectedBeforeBuildingRequest() {
        myFixture.configureByText("Async.kt", """
                package sample
                class Async {
                    suspend fun <caret>run(value: String) = value
                }
                """);
        try {
            MethodInvocationSnapshot.capture(lightMethodAtCaret());
            fail("suspend 方法应明确拒绝，不能发出缺少 continuation 的请求");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    /** 从实际菜单入口拒绝 suspend 时，只发普通通知，不产生 IDE 插件错误或更新再次调用记录。 */
    public void testSuspendOpenActionOnlyShowsInformation() {
        configureSuspendMethod();
        List<Notification> notifications = captureNotifications();
        String projectName = getProject().getName();
        PsiMethod previous = AnyDoorAgainOpenAction.PRE_METHOD_MAP.remove(projectName);
        try {
            new AnyDoorOpenAction().actionPerformed(actionEvent());
            assertSuspendInformation(notifications);
            assertFalse(AnyDoorAgainOpenAction.PRE_METHOD_MAP.containsKey(projectName));
        } finally {
            restorePreviousMethod(projectName, previous);
        }
    }

    /** 缓存方法后来变为 suspend 时，再次调用入口应采用相同的预期拒绝处理。 */
    public void testSuspendAgainActionOnlyShowsInformation() {
        PsiMethod method = configureSuspendMethod();
        List<Notification> notifications = captureNotifications();
        String projectName = getProject().getName();
        PsiMethod previous = AnyDoorAgainOpenAction.PRE_METHOD_MAP.put(projectName, method);
        try {
            new AnyDoorAgainOpenAction().actionPerformed(actionEvent());
            assertSuspendInformation(notifications);
            assertSame(method, AnyDoorAgainOpenAction.PRE_METHOD_MAP.get(projectName));
        } finally {
            restorePreviousMethod(projectName, previous);
        }
    }

    /** 历史参数入口同样在打开窗口和执行回调前拒绝 suspend。 */
    public void testSuspendHistoryOnlyShowsInformation() {
        PsiMethod method = configureSuspendMethod();
        List<Notification> notifications = captureNotifications();
        new AnyDoorPerformed().doUseNewUI(new AnyDoorSettingsState(), getProject(),
                method.getContainingClass(), method,
                AnyDoorActionUtil.genCacheKey(method.getContainingClass(), method),
                () -> fail("suspend 拒绝后不能执行成功回调"), null);
        assertSuspendInformation(notifications);
    }

    private PsiMethod configureSuspendMethod() {
        myFixture.configureByText("Async.kt", """
                package sample
                class Async {
                    suspend fun <caret>run(value: String) = value
                }
                """);
        return lightMethodAtCaret();
    }

    private List<Notification> captureNotifications() {
        List<Notification> notifications = new ArrayList<>();
        getProject().getMessageBus().connect(getTestRootDisposable()).subscribe(Notifications.TOPIC, new Notifications() {
            @Override
            public void notify(Notification notification) {
                notifications.add(notification);
            }
        });
        return notifications;
    }

    private void assertSuspendInformation(List<Notification> notifications) {
        assertEquals("预期只出现一条 suspend 能力提示", 1, notifications.size());
        Notification notification = notifications.get(0);
        assertEquals(NotificationType.INFORMATION, notification.getType());
        assertTrue(notification.getContent().contains(AnyDoorBundle.message("method.suspend.unsupported")));
        notification.expire();
    }

    private static void restorePreviousMethod(String projectName, PsiMethod previous) {
        if (previous == null) {
            AnyDoorAgainOpenAction.PRE_METHOD_MAP.remove(projectName);
        } else {
            AnyDoorAgainOpenAction.PRE_METHOD_MAP.put(projectName, previous);
        }
    }

    public void testDtoTemplateCompletionAndSourceNavigation() {
        PsiFile kotlinFile = myFixture.configureByText("RequestService.kt", """
                package sample
                data class Request(val title: String, val count: Int)
                class RequestService {
                    fun <caret>run(request: Request) = request.title
                }
                """);
        PsiMethod method = lightMethodAtCaret();
        assertEquals("{\"request\":{\"title\":\"\",\"count\":0}}",
                JsonElementUtil.toParamNameListNew(method.getParameterList()).toString());
        myFixture.configureByText(JSONEditor.ANY_DOOR_PARAM_FILE_NAME, "{\"<caret>request\":{}}");
        myFixture.getFile().putUserData(JSONEditor.ANY_DOOR_EDIT_PARAMS, method.getParameterList());
        PsiElement target = new JsonKeyNavigationProvider().getNavigationElement(
                myFixture.getFile().findElementAt(myFixture.getCaretOffset()));
        assertNotNull(target);
        assertEquals(kotlinFile, target.getContainingFile());
        myFixture.configureByText(JSONEditor.ANY_DOOR_PARAM_FILE_NAME, "{\"<caret>\" : null}");
        myFixture.getEditor().putUserData(JSONEditor.ANY_DOOR_EDIT_PARAMS, method.getParameterList());
        myFixture.completeBasic();
        List<String> variants = myFixture.getLookupElementStrings();
        assertNotNull(variants);
        assertTrue(variants.contains("request"));
        assertTrue(variants.contains("title"));
        assertTrue(variants.contains("count"));
    }

    public void testLocalFunctionDoesNotInvokeItsEnclosingMethod() {
        myFixture.configureByText("Local.kt", """
                package sample
                class Local {
                    fun run() {
                        fun <caret>local() = "local"
                    }
                }
                """);
        assertNull(IdeClassUtil.findMethod(myFixture.getFile().findElementAt(myFixture.getCaretOffset())));
    }

    public void testCaretInsideLambdaFindsItsEnclosingMethod() {
        myFixture.configureByText("LambdaService.kt", """
                package sample
                class LambdaService {
                    fun run() = listOf("value").map { <caret>it.uppercase() }
                }
                """);
        PsiMethod method = IdeClassUtil.findMethod(myFixture.getFile().findElementAt(myFixture.getCaretOffset()));
        assertNotNull("方法体内的 lambda 应继续定位到所属方法", method);
        assertEquals("run", method.getName());
    }

    public void testJavaNestedParameterKeepsExistingCacheKey() {
        myFixture.configureByText("Outer.java", """
                package sample;
                class Outer {
                    static class Request {}
                    void <caret>run(Request request) {}
                }
                """);
        PsiMethod method = IdeClassUtil.findMethod(myFixture.getFile().findElementAt(myFixture.getCaretOffset()));
        MethodInvocationSnapshot snapshot = MethodInvocationSnapshot.capture(method);
        assertEquals(List.of("sample.Outer$Request"), snapshot.parameterTypeNames());
        assertEquals("sample.Outer#run#sample.Outer.Request", snapshot.cacheKey());
        assertEquals(AnyDoorActionUtil.genCacheKey(method.getContainingClass(), method), snapshot.cacheKey());
    }

    public void testJavaGenericSignatureUsesJvmErasure() {
        myFixture.configureByText("GenericService.java", """
                package sample;
                import java.util.List;
                class GenericService {
                    <T extends Number> void <caret>run(List<String> names, T value, String[] values) {}
                }
                """);
        PsiMethod method = IdeClassUtil.findMethod(myFixture.getFile().findElementAt(myFixture.getCaretOffset()));
        MethodInvocationSnapshot snapshot = MethodInvocationSnapshot.capture(method);
        assertEquals(List.of("java.util.List", "java.lang.Number", "java.lang.String[]"), snapshot.parameterTypeNames());
    }

    private AnActionEvent actionEvent() {
        return AnActionEvent.createFromDataContext("test", null, SimpleDataContext.builder()
                .add(CommonDataKeys.PROJECT, getProject())
                .add(CommonDataKeys.EDITOR, myFixture.getEditor())
                .add(CommonDataKeys.PSI_FILE, myFixture.getFile())
                .build());
    }

    private PsiMethod lightMethodAtCaret() {
        PsiElement element = myFixture.getFile().findElementAt(myFixture.getCaretOffset());
        while (element != null) {
            UMethod method = UastContextKt.toUElement(element, UMethod.class);
            if (method != null) {
                return method.getJavaPsi();
            }
            element = element.getParent();
        }
        fail("未定位到真实 Kotlin 方法");
        return null;
    }
}
