package io.github.lgp547.anydoorplugin.dialog.components;

import java.awt.*;
import java.util.function.Consumer;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBDimension;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 11:11
 **/
public class SaveDialog extends DialogWrapper {

    private final JBTextField textField;

    private final Consumer<SaveDialog> consumer;

    public SaveDialog(@Nullable Project project, Consumer<SaveDialog> consumer) {
        super(project, true, IdeModalityType.IDE);
        this.textField = new JBTextField();
        this.consumer = consumer;

        init();
    }

    @Override
    protected void doOKAction() {
        if (doValidate() != null) {
            return;
        }
        consumer.accept(this);
        super.doOKAction();
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (StringUtils.isBlank(textField.getText())) {
            return new ValidationInfo("Name is empty", textField);
        }
        return super.doValidate();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new JBDimension(350, 30));

        textField.getEmptyText().setText("Please enter record name");

        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    public void setName(String name) {
        textField.setText(name);
    }

    public String getName() {
        return textField.getText();
    }
}
