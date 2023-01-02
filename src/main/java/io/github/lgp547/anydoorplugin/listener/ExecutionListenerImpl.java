package io.github.lgp547.anydoorplugin.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import io.github.lgp547.anydoorplugin.util.ImportUtil;
import org.jetbrains.annotations.NotNull;

public class ExecutionListenerImpl implements ExecutionListener {

    @Override
    public void processStartScheduled(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
        ImportUtil.fillAnyDoorJar(env);
    }
}
