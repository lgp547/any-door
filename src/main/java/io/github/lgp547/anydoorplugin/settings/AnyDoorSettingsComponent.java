package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import io.github.lgp547.anydoorplugin.util.ImportUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * java Swing
 */
public class AnyDoorSettingsComponent {

    private final JPanel myMainPanel;

    private final JBTextField anyDoorPortText = new JBTextField();

    private final JBTextField versionText = new JBTextField();

    private final JBCheckBox enableAutoFill = new JBCheckBox();

    private final JBCheckBox enableAsyncExecute = new JBCheckBox();

    private final ComboBox<String> mainClassModuleComboBox = new ComboBox<>();

    private final JBTextField webPathPrefix = new JBTextField();

    private final JBTextField projectPid = new JBTextField();

    private final JBRadioButton runProjectModeRadio1 = new JBRadioButton("Java attach");

    private final JBRadioButton runProjectModeRadio2 = new JBRadioButton("Spring mvc");


    public AnyDoorSettingsComponent(Project project) {
        JButton button = new JButton("Try import `any-door` jar to module");
        button.addActionListener(e -> {
            String version = versionText.getText();
            String runModuleName = mainClassModuleComboBox.getItem();
            if (StringUtils.isBlank(version)) {
                NotifierUtil.notifyError(project, "Please fill version");
                return;
            }
            ImportUtil.fillAnyDoorJar(project, runModuleName, version);
        });

        Module[] modules = ModuleManager.getInstance(project).getModules();
        Arrays.stream(modules).map(Module::getName).sorted().forEach(mainClassModuleComboBox::addItem);
        mainClassModuleComboBox.setItem("start");

        AnyDoorSettingsState settings = project.getService(AnyDoorSettingsState.class);
        projectPid.setText(String.valueOf(settings.pid));


        ButtonGroup runProjectModeGroup = new ButtonGroup();
        runProjectModeGroup.add(runProjectModeRadio1);
        runProjectModeGroup.add(runProjectModeRadio2);
        JPanel runProjectModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        runProjectModePanel.add(runProjectModeRadio1);
        runProjectModePanel.add(runProjectModeRadio2);
        runProjectModeRadio1.setSelected(true);

        JPanel runProjectModePanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        runProjectModePanel2.add(mainClassModuleComboBox);
        runProjectModePanel2.add(button);

        // JAR -> any-door jar
        // Execute -> java attach ｜ spring mvc
        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enable auto fill `any-door` jar:"), enableAutoFill)
                .addLabeledComponent(new JBLabel("Fill `any-door` jar version:"), versionText)
                .addLabeledComponent(new JBLabel("Select Main class module name:"), runProjectModePanel2)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Enable async execute `any-door`:"), enableAsyncExecute)
                .addLabeledComponent(new JBLabel("Select execute `any-door` mode:"), runProjectModePanel)
                .addLabeledComponent(new JBLabel("'Java attach' pid:"), projectPid)
                .addLabeledComponent(new JBLabel("'Spring mvc' port:"), anyDoorPortText)
                .addLabeledComponent(new JBLabel("'Spring mvc' context-path:"), webPathPrefix)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return anyDoorPortText;
    }

    @NotNull
    public String getAnyDoorPortText() {
        return anyDoorPortText.getText();
    }

    public void setAnyDoorPortText(@NotNull String newText) {
        anyDoorPortText.setText(newText);
    }

    public Boolean getEnableAutoFill() {
        return enableAutoFill.isSelected();
    }

    public void setEnableAutoFill(boolean newStatus) {
        enableAutoFill.setSelected(newStatus);
    }


    public Boolean getEnableAsyncExecute() {
        return enableAsyncExecute.isSelected();
    }

    public void setEnableAsyncExecute(boolean newStatus) {
        enableAsyncExecute.setSelected(newStatus);
    }


    @NotNull
    public String getVersionText() {
        return versionText.getText();
    }

    public void setVersionText(@NotNull String newText) {
        versionText.setText(newText);
    }

    public void setMainClassModuleText(String text) {
        mainClassModuleComboBox.setItem(text);
    }

    public String getMainClassModuleText() {
        return mainClassModuleComboBox.getItem();
    }

    public String getWebPathPrefix() {
        String text = webPathPrefix.getText();
        if (StringUtils.isBlank(text)) {
            return "";
        }
        if (!StringUtils.startsWith(text, "/")) {
            text = "/" + text;
        }
        return StringUtils.removeEnd(text, "/");
    }

    public void setWebPathPrefix(String text) {
        webPathPrefix.setText(text);
    }

    public String getProjectPid() {
        return projectPid.getText();
    }

    public void setProjectPid(String text) {
        projectPid.setText(text);
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