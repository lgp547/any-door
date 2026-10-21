package io.github.lgp547.anydoorplugin.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.BaseProcessHandler;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import io.github.lgp547.anydoorplugin.util.RuntimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalLong;
import java.util.function.LongSupplier;

public class ExecutionListenerImpl implements ExecutionListener {

    private static final Logger log = Logger.getInstance(ExecutionListenerImpl.class);
    /** 用类名识别 Kotlin 配置，避免让可选 Kotlin 插件成为生产代码依赖。 */
    private static final String KOTLIN_RUN_CONFIGURATION_CLASS = "org.jetbrains.kotlin.idea.run.KotlinRunConfiguration";

    @Override
    public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        fillRunProjectPid(env, handler);
    }

    private void fillRunProjectPid(@NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        Project project = env.getProject();
        try {
            String runClassName = env.getRunProfile().getClass().getName();
            OptionalLong pid = resolvePid(runClassName, handler, () -> findProjectPid(project, env));
            if (pid.isEmpty()) {
                log.debug("fillRunProjectPid runClassName is not match {}", runClassName);
                return;
            }
            AnyDoorSettingsState.getAnyDoorSettingsState(project).pid = pid.getAsLong();
        } catch (Exception e) {
            NotifierUtil.notifyError(project, "fill run project pid fail: " + e.getMessage());
        }
    }

    static OptionalLong resolvePid(String runClassName, ProcessHandler handler, LongSupplier fallbackPid) {
        if (KOTLIN_RUN_CONFIGURATION_CLASS.equals(runClassName)) {
            if (handler instanceof BaseProcessHandler<?> processHandler) {
                return OptionalLong.of(processHandler.getProcess().pid());
            }
            return OptionalLong.of(fallbackPid.getAsLong());
        }
        if (!isJavaOrSpringRunConfiguration(runClassName)) {
            return OptionalLong.empty();
        }
        if (handler instanceof KillableColoredProcessHandler.Silent processHandler) {
            return OptionalLong.of(processHandler.getProcess().pid());
        }
        log.debug("fillRunProjectPid !handler instanceof KillableColoredProcessHandler.Silent");
        return OptionalLong.of(fallbackPid.getAsLong());
    }

    private static boolean isJavaOrSpringRunConfiguration(String runClassName) {
        return StringUtils.endsWith(runClassName, "SpringBootApplicationRunConfiguration")
                || StringUtils.endsWithIgnoreCase(runClassName, "ApplicationConfiguration");
    }

    private static long findProjectPid(Project project, ExecutionEnvironment env) {
        String basePath = "";
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            basePath = projectSdk.getHomePath() + "/bin/";
        }
        String projectPid = RuntimeUtil.getProjectPid(basePath, env.getRunProfile().getName());
        return NumberUtils.toLong(projectPid);
    }
}
