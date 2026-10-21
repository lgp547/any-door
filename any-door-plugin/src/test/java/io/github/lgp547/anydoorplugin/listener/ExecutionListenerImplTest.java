package io.github.lgp547.anydoorplugin.listener;

import com.intellij.execution.process.BaseProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import junit.framework.TestCase;
import org.junit.Assume;

import java.nio.charset.Charset;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * 验证受支持的运行配置能够获取目标进程 PID，且不会把无关配置误当成项目进程。
 *
 * @author luoweiyu
 * @date 2026-10-20
 */
public class ExecutionListenerImplTest extends TestCase {

    /** Kotlin 配置应从平台的标准 OS 进程 handler 取得 PID，而不调用名称搜索回退。 */
    public void testKotlinRunConfigurationUsesProcessHandlerPid() throws Exception {
        String kotlinRunConfiguration = "org.jetbrains.kotlin.idea.run.KotlinRunConfiguration";
        try {
            Class.forName(kotlinRunConfiguration, false, getClass().getClassLoader());
        } catch (ClassNotFoundException kotlinPluginUnavailable) {
            Assume.assumeTrue("Kotlin plugin is not available in this test runtime", false);
        }
        Process process = new ProcessBuilder(javaExecutable(), "-version").start();
        try {
            ProcessHandler handler = new TestProcessHandler(process);
            OptionalLong pid = ExecutionListenerImpl.resolvePid(
                    kotlinRunConfiguration,
                    handler,
                    () -> {
                        fail("Kotlin 配置应直接读取进程 PID");
                        return -1L;
                    }
            );

            assertTrue(pid.isPresent());
            assertEquals(process.pid(), pid.getAsLong());
        } finally {
            process.destroyForcibly();
        }
    }

    /** 原有 Java 与 Spring 配置仍进入 jps PID 回退路径。 */
    public void testJavaAndSpringConfigurationsKeepExistingFallback() {
        assertLegacyConfigurationUsesFallback("some.ApplicationConfiguration");
        assertLegacyConfigurationUsesFallback("some.SpringBootApplicationRunConfiguration");
    }

    /** 无关配置必须短路，不读取进程 PID，也不调用 jps 回退。 */
    public void testUnrelatedConfigurationDoesNotReadPidOrInvokeFallback() {
        AtomicBoolean fallbackInvoked = new AtomicBoolean();
        OptionalLong pid = ExecutionListenerImpl.resolvePid(
                "some.OtherRunConfiguration",
                null,
                () -> {
                    fallbackInvoked.set(true);
                    return 123L;
                }
        );

        assertFalse(pid.isPresent());
        assertFalse(fallbackInvoked.get());
    }

    private static void assertLegacyConfigurationUsesFallback(String runConfigurationClassName) {
        AtomicBoolean fallbackInvoked = new AtomicBoolean();
        OptionalLong pid = ExecutionListenerImpl.resolvePid(
                runConfigurationClassName,
                null,
                () -> {
                    fallbackInvoked.set(true);
                    return 123L;
                }
        );

        assertTrue(pid.isPresent());
        assertEquals(123L, pid.getAsLong());
        assertTrue(fallbackInvoked.get());
    }

    private static String javaExecutable() {
        return System.getProperty("java.home") + "/bin/java";
    }

    private static final class TestProcessHandler extends BaseProcessHandler<Process> {

        private TestProcessHandler(Process process) {
            super(process, "java -version", Charset.defaultCharset());
        }

        @Override
        public Future<?> executeTask(Runnable task) {
            return CompletableFuture.runAsync(task);
        }
    }
}
