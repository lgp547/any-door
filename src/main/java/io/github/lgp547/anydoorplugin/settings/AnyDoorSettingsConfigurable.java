package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AnyDoorSettingsConfigurable implements Configurable {

    private AnyDoorSettingsComponent mySettingsComponent;

    private Project project;


    public AnyDoorSettingsConfigurable(@NotNull Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Any Door";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return mySettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new AnyDoorSettingsComponent(project);
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AnyDoorSettingsState settings = project.getService(AnyDoorSettingsState.class);
        return !mySettingsComponent.getAnyDoorPortText().equals(String.valueOf(settings.port)) ||
                !mySettingsComponent.isSelectJavaAttach().equals(settings.isSelectJavaAttach()) ||
                !mySettingsComponent.getVersionText().equals(settings.version) ||
                !mySettingsComponent.getEnableAutoFill().equals(settings.enableAutoFill) ||
                !mySettingsComponent.getEnableAsyncExecute().equals(settings.enableAsyncExecute) ||
//                !mySettingsComponent.getMainClassModuleText().equals(settings.runModule) ||
                !mySettingsComponent.getWebPathPrefix().equals(settings.webPathPrefix) ||
                !mySettingsComponent.getProjectPid().equals(String.valueOf(settings.pid))
                ;
    }

    @Override
    public void apply() {
        AnyDoorSettingsState settings = project.getService(AnyDoorSettingsState.class);
        String anyDoorPortText = mySettingsComponent.getAnyDoorPortText();
        settings.port = NumberUtils.toInt(anyDoorPortText, settings.port);
        settings.pid = NumberUtils.toLong(mySettingsComponent.getProjectPid(), settings.pid);
        settings.updateRunProjectEnum(mySettingsComponent.isSelectJavaAttach());

        settings.enableAutoFill = mySettingsComponent.getEnableAutoFill();
        settings.enableAsyncExecute = mySettingsComponent.getEnableAsyncExecute();
        settings.runModule = mySettingsComponent.getPluginLibNames();
        settings.webPathPrefix = mySettingsComponent.getWebPathPrefix();

        if (!settings.updateVersion(mySettingsComponent.getVersionText())) {
            mySettingsComponent.setVersionText(settings.version);
        }
    }

    @Override
    public void reset() {
        AnyDoorSettingsState settings = project.getService(AnyDoorSettingsState.class);
        mySettingsComponent.setAnyDoorPortText(String.valueOf(settings.port));
        mySettingsComponent.updateSelectJavaAttach(settings.isSelectJavaAttach());
        mySettingsComponent.setEnableAutoFill(settings.enableAutoFill);
        mySettingsComponent.setEnableAsyncExecute(settings.enableAsyncExecute);
        mySettingsComponent.setVersionText(settings.version);
        mySettingsComponent.setPluginLibNames(settings.runModule);
        mySettingsComponent.setWebPathPrefix(settings.webPathPrefix);
        mySettingsComponent.setProjectPid(String.valueOf(settings.pid));
    }

    @Override
    public void disposeUIResources() {
        project = null;
        mySettingsComponent = null;
    }

}