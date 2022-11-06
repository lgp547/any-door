// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package io.github.lgp547.anydoorplugin.settings;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import io.github.lgp547.anydoorplugin.util.ImportUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class AnyDoorSettingsComponent {

  private final JPanel myMainPanel;
  private final JBTextField anyDoorPortText = new JBTextField();
  private final JBTextField versionText = new JBTextField();
  private final JBTextField mainClassModuleText = new JBTextField();
  private final JBCheckBox enableAnyDoorBox = new JBCheckBox("Enable any-door");

  public AnyDoorSettingsComponent(Project project) {
    JButton button = new JButton("Try import jar to RunModule");
    button.addActionListener(e -> {
      String version = versionText.getText();
//      String version = project.getService(AnyDoorSettingsState.class).version;
      String runModuleName = mainClassModuleText.getText();
      if (StringUtils.isAnyBlank(version, runModuleName)) {
        NotifierUtil.notifyError(project, "Please fill RunModule and version");
        return;
      }
      ImportUtil.fillAnyDoorJar(project, runModuleName, version);
    });

    JBLabel label = new JBLabel("Main class RunModule name:");
//    label.setToolTipText("Not required");
    myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Run project port:"), anyDoorPortText, 1, false)
            .addLabeledComponent(new JBLabel("Any-door jar version:"), versionText, 1, false)
            .addLabeledComponent(label, mainClassModuleText, 1, false)
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

  public Boolean getEnableAnyDoorBox() {
    return enableAnyDoorBox.isSelected();
  }

  public void setEnableAnyDoorBox(boolean newStatus) {
    enableAnyDoorBox.setSelected(newStatus);
  }

  @NotNull
  public String getVersionText() {
    return versionText.getText();
  }

  public void setVersionText(@NotNull String newText) {
    versionText.setText(newText);
  }

  public void setMainClassModuleText(String text) {
    mainClassModuleText.setText(text);
  }

  public String getMainClassModuleText() {
    return mainClassModuleText.getText();
  }
}