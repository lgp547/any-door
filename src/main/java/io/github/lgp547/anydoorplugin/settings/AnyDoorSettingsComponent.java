package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import io.github.lgp547.anydoorplugin.util.ImportNewUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * java Swing
 */
public class AnyDoorSettingsComponent {

    private JPanel myMainPanel;

    private final JBCheckBox enableAsyncExecute = new JBCheckBox();

    private final JBTextField projectPid = new JBTextField();

    private final ComboBox<String> dependenceNames = new ComboBox<>();

    private final JBTextField dependenceVersion = new JBTextField();

    private final JButton button = new JButton("Idea plugins base path");

    private final JBTextField mvcAddressText = new JBTextField();

    private final JBTextField mvcPortText = new JBTextField();

    private final JBTextField mvcWebPathPrefix = new JBTextField();

    private final JBRadioButton runProjectModeRadio1 = new JBRadioButton("Java attach");

    private final JBRadioButton runProjectModeRadio2 = new JBRadioButton("Spring mvc");

    private final ComboBox<String> mainClassModuleComboBox = new ComboBox<>();

    private final JButton importButton = new JButton("Try import `any-door` jar to module");

    public AnyDoorSettingsComponent(Project project) {
        componentInit(project);
        mainPanelInit();
    }

    private void mainPanelInit() {
//        JPanel runProjectModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
//        runProjectModePanel.add(dependenceNames);
//        runProjectModePanel.add(dependenceVersion);

        JPanel runProjectModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        runProjectModePanel.add(runProjectModeRadio1);
        runProjectModePanel.add(runProjectModeRadio2);

        JPanel runProjectModePanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        runProjectModePanel2.add(mainClassModuleComboBox);
        runProjectModePanel2.add(importButton);

        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enable async execute:"), enableAsyncExecute)
                .addComponent(button)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Select execute `any-door` mode:"), runProjectModePanel)
                .addLabeledComponent(new JBLabel("'Java attach' pid:"), projectPid)
                .addLabeledComponent(new JBLabel("'Spring mvc' address:"), mvcAddressText)
                .addLabeledComponent(new JBLabel("'Spring mvc' port:"), mvcPortText)
                .addLabeledComponent(new JBLabel("'Spring mvc' context-path:"), mvcWebPathPrefix)
                .addLabeledComponent(new JBLabel("'Spring mvc' run main module:"), runProjectModePanel2)
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

        button.addActionListener(e -> NotifierUtil.notifyInfo(project, PathManager.getPluginsPath()));

        ButtonGroup runProjectModeGroup = new ButtonGroup();
        runProjectModeGroup.add(runProjectModeRadio1);
        runProjectModeGroup.add(runProjectModeRadio2);
        runProjectModeRadio1.setSelected(true);

        Module[] modules = ModuleManager.getInstance(project).getModules();
        Arrays.stream(modules).map(Module::getName).sorted().forEach(mainClassModuleComboBox::addItem);
        mainClassModuleComboBox.setItem("start");

        importButton.addActionListener(e -> ImportNewUtil.fillAnyDoorJar(project, mainClassModuleComboBox.getItem()));
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

    public String getMvcAddress() {
        return mvcAddressText.getText();
    }

    public void setMvcAddressText(String mvcAddressText) {
        this.mvcAddressText.setText(mvcAddressText);
    }

    public Integer getMvcPort() {
        return NumberUtils.toInt(mvcPortText.getText(), -1);
    }

    public void setMvcPortText(Integer mvcPortText) {
        this.mvcPortText.setText(String.valueOf(mvcPortText));
    }

    public String getMvcWebPathPrefix() {
        String text = mvcWebPathPrefix.getText();
        if (StringUtils.isBlank(text)) {
            return "";
        }
        if (!StringUtils.startsWith(text, "/")) {
            text = "/" + text;
        }
        return StringUtils.removeEnd(text, "/");
    }

    public void setMvcWebPathPrefix(String text) {
        mvcWebPathPrefix.setText(text);
    }

    public Boolean isSelectJavaAttach() {
        return runProjectModeRadio1.isSelected();
    }

    public void updateSelectJavaAttach(Boolean selectJavaAttach) {
        if (selectJavaAttach) {
            runProjectModeRadio1.setSelected(true);
        } else {
            runProjectModeRadio2.setSelected(true);
        }
    }
}