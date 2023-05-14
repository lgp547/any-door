package io.github.lgp547.anydoorplugin.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.ImportNewUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import io.github.lgp547.anydoorplugin.util.RuntimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

public class ExecutionListenerImpl implements ExecutionListener {

    private static final Logger log = Logger.getInstance(ExecutionListenerImpl.class);

    @Override
    public void processStartScheduled(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
        ImportNewUtil.checkAndGenAnyDoorJarAllDependence(env.getProject());
    }

    @Override
    public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        fillRunProjectPid(env, handler);
    }

    private void fillRunProjectPid(@NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        Project project = env.getProject();
        try {
            String runClassName = env.getRunProfile().getClass().getName();
            if (!StringUtils.endsWith(runClassName, "SpringBootApplicationRunConfiguration") && !StringUtils.endsWithIgnoreCase(runClassName, "ApplicationConfiguration")) {
                log.debug("fillRunProjectPid runClassName is not match {}", runClassName);
                return;
            }
            AnyDoorSettingsState anyDoorSettingsState = AnyDoorSettingsState.getAnyDoorSettingsState(project);
            if (handler instanceof KillableColoredProcessHandler.Silent) {
                anyDoorSettingsState.pid = ((KillableColoredProcessHandler.Silent) handler).getProcess().pid();
            } else {
                log.debug("fillRunProjectPid !handler instanceof KillableColoredProcessHandler.Silent");
                // jps
                String basePath = "";
                Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
                if (null != projectSdk) {
                    basePath = projectSdk.getHomePath() + "/bin/";
                }
                String projectPid = RuntimeUtil.getProjectPid(basePath, env.getRunProfile().getName());
                anyDoorSettingsState.pid = NumberUtils.toLong(projectPid);
            }
        } catch (Exception e) {
            NotifierUtil.notifyError(project, "fill run project pid fail: " + e.getMessage());
        }
    }
}
