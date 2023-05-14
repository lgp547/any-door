package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * A new one is constructed each time the configuration page is opened
 */
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
        if (null == mySettingsComponent) {
            mySettingsComponent = new AnyDoorSettingsComponent(project);
        }
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AnyDoorSettingsState settings = project.getService(AnyDoorSettingsState.class);
        return !mySettingsComponent.getDependenceVersion().equals(settings.dependenceVersion) ||
                !mySettingsComponent.getEnableAsyncExecute().equals(settings.enableAsyncExecute) ||
                !mySettingsComponent.getProjectPid().equals(String.valueOf(settings.pid))
                ;
    }

    @Override
    public void apply() {
        AnyDoorSettingsState settings = project.getService(AnyDoorSettingsState.class);
        settings.pid = NumberUtils.toLong(mySettingsComponent.getProjectPid(), settings.pid);
        settings.enableAsyncExecute = mySettingsComponent.getEnableAsyncExecute();
        if (!settings.updateDependence(mySettingsComponent.getDependenceNames(), mySettingsComponent.getDependenceVersion())) {
            mySettingsComponent.setDependenceVersion(settings.dependenceVersion);
        }
    }

    @Override
    public void reset() {
        AnyDoorSettingsState settings = project.getService(AnyDoorSettingsState.class);
        mySettingsComponent.setEnableAsyncExecute(settings.enableAsyncExecute);
        mySettingsComponent.setDependenceVersion(settings.dependenceVersion);
        mySettingsComponent.setDependenceNames(settings.dependenceName);
        mySettingsComponent.setProjectPid(String.valueOf(settings.pid));
    }

    @Override
    public void disposeUIResources() {
        project = null;
        mySettingsComponent = null;
    }

}