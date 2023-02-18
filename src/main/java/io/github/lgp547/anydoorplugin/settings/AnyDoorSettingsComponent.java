package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import io.github.lgp547.anydoorplugin.util.ImportUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * java Swing
 */
public class AnyDoorSettingsComponent{

    private final JPanel myMainPanel;

    private final JBTextField anyDoorPortText = new JBTextField();

    private final JBTextField versionText = new JBTextField();

    private final JBCheckBox enableAutoFill = new JBCheckBox();

    private final JBCheckBox enableAsyncExecute = new JBCheckBox();

    private final ComboBox<String> mainClassModuleComboBox = new ComboBox<>();

    private final JBTextField webPathPrefix = new JBTextField();// tmp


    public AnyDoorSettingsComponent(Project project) {
        JButton button = new JButton("Try import jar to RunModule");
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
        for (Module module : modules) {
            mainClassModuleComboBox.addItem(module.getName());
        }
        mainClassModuleComboBox.setItem("start");

        myMainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Project servlet port:"), anyDoorPortText, 1, false)
                .addLabeledComponent(new JBLabel("Project servlet context-path:"), webPathPrefix, 1, false)
                .addLabeledComponent(new JBLabel("Any-door jar version:"), versionText, 1, false)
                .addLabeledComponent(new JBLabel("Enable auto fill Any-door jar:"), enableAutoFill, 1, false)
                .addLabeledComponent(new JBLabel("Enable async execute:"),enableAsyncExecute,1,false)
                .addLabeledComponent(new JBLabel("Main class RunModule name:"), mainClassModuleComboBox, 1, false)
                .addComponent(button)
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
}