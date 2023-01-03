package io.github.lgp547.anydoorplugin.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.ImportUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExecutionListenerImpl implements ExecutionListener {

    @Override
    public void processStartScheduled(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
        ImportUtil.fillAnyDoorJar(env);
        fillAnyDoorPort(env);
    }

    private void fillAnyDoorPort(ExecutionEnvironment env) {
        if (env.getRunProfile() instanceof ApplicationConfiguration) {
            ApplicationConfiguration runProfile = (ApplicationConfiguration) env.getRunProfile();
            String str = "-Danydoor.port=";
            if (StringUtils.contains(runProfile.getVMParameters(), str)) {
                String port = StringUtils.substringBetween(runProfile.getVMParameters(), str, " ");
                synPort(NumberUtils.toInt(port, -1), env);
            } else {
                Integer port = getPort();
                synPort(port, env);
                runProfile.setVMParameters(runProfile.getVMParameters() + " " + str + port);
            }
        }
    }

    /**
     * syn to AnyDoorSettingsState
     */
    private void synPort(Integer port, ExecutionEnvironment env) {
        if (port == -1) {
            return;
        }
        Project project = env.getProject();
        Optional<AnyDoorSettingsState> anyDoorSettingsStateNotExc = AnyDoorSettingsState.getAnyDoorSettingsStateNotExc(project);
        anyDoorSettingsStateNotExc.ifPresent(anyDoorSettingsState -> anyDoorSettingsState.port = port);
    }

    /**
     * @return available port
     */
    private Integer getPort() {
        // todo:
        return 6666;
    }
}
