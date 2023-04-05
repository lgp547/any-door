package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * java Swing
 */
public class AnyDoorSettingsComponent {

    private JPanel myMainPanel;

    private final JBCheckBox enableAsyncExecute = new JBCheckBox();

    private final JBTextField projectPid = new JBTextField();

    private final ComboBox<String> dependenceNames = new ComboBox<>();

    private final JBTextField dependenceVersion = new JBTextField();

//    private final JButton button = new JButton("Try update dependence version");

    public AnyDoorSettingsComponent(Project project) {
        componentInit(project);
        mainPanelInit();
    }

    private void mainPanelInit() {
//        JPanel runProjectModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
//        runProjectModePanel.add(dependenceNames);
//        runProjectModePanel.add(dependenceVersion);

        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enable async execute:"), enableAsyncExecute)
                .addLabeledComponent(new JBLabel("Project run pid:"), projectPid)
//                .addSeparator()
//                .addLabeledComponent(new JBLabel("Fill dependence name and version:"), runProjectModePanel)
//                .addComponent(button)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void componentInit(Project project) {
//        button.addActionListener(e -> {
//            String dependenceName = dependenceNames.getItem();
//            String dependenceVersion = this.dependenceVersion.getText();
//            if (StringUtils.isBlank(dependenceName) || StringUtils.isBlank(dependenceVersion)) {
//                NotifierUtil.notifyError(project, "Please fill dependence info");
//                return;
//            }
//            ImportNewUtil.fillAnyDoorJar(project, dependenceName, dependenceVersion);
//        });

//        AnyDoorInfo.libMap.keySet().forEach(dependenceNames::setItem);
        AnyDoorSettingsState settings = project.getService(AnyDoorSettingsState.class);
        projectPid.setText(String.valueOf(settings.pid));
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return dependenceVersion;
    }

    public Boolean getEnableAsyncExecute() {
        return enableAsyncExecute.isSelected();
    }

    public void setEnableAsyncExecute(boolean newStatus) {
        enableAsyncExecute.setSelected(newStatus);
    }

    @NotNull
    public String getDependenceVersion() {
        return dependenceVersion.getText();
    }

    public void setDependenceVersion(@NotNull String newText) {
        dependenceVersion.setText(newText);
    }

    public void setDependenceNames(String text) {
        dependenceNames.setItem(text);
    }

    public String getDependenceNames() {
        return dependenceNames.getItem();
    }

    public String getProjectPid() {
        return projectPid.getText();
    }

    public void setProjectPid(String text) {
        projectPid.setText(text);
    }

}