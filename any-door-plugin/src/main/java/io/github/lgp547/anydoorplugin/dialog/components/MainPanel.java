package io.github.lgp547.anydoorplugin.dialog.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBDimension;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.JSONEditor;
import io.github.lgp547.anydoorplugin.dialog.MethodDataContext;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.impl.ImportExportEvent;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.util.JsonUtil;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-19 18:42
 **/
public class MainPanel extends JBPanel<MainPanel> implements Listener {

    private Project project;
    private final MyToolBar toolBar;
    private final MyEditor editor;
    private final MyComboBox comboBox;
    private final MethodDataContext context;
    private final JButton saveParamButton;
    private final JBIntSpinner runNum;
    private final JBCheckBox isConcurrent;

    public MainPanel(Project project, MethodDataContext context) {
        super(new GridBagLayout());
        setPreferredSize(new JBDimension(670, 500));

        this.project = project;
        this.context = context;

        toolBar = new MyToolBar(project, context);
        editor = new MyEditor(context, context.cacheContent, context.getParamList(), project);
        comboBox = new MyComboBox(project, context);
        saveParamButton = new JButton("Save");
        runNum = new JBIntSpinner(1, 1, Integer.MAX_VALUE);
        isConcurrent = new JBCheckBox("", true);

        this.context.addListener(this);
        this.context.addListener(comboBox);
        this.context.addListener(editor);

        context.fireEvent(EventHelper.createDisplayDataChangeEvent(context.listDisplayData(), context.getSelectedItem()));

        initComponent();
        componentLayout();
    }

    private void initComponent() {
        saveParamButton.addActionListener(e -> {
            if (Objects.isNull(context.getClazz())) {
                throw new RuntimeException("Current Class Not Exist");
            }

            JSONEditor editor = this.getEditor();
            String text = JsonUtil.compressJson(editor.getText());

            ParamDataItem selectedDataItem = context.getSelectedItem();
            selectedDataItem.setParam(text);

            if (StringUtils.isBlank(selectedDataItem.getName())) {
                new SaveDialog(project, (dialog) -> {
                    String name = dialog.getName().trim();
                    selectedDataItem.setName(name);
                    context.flush();
                }).show();
            } else {
                context.flush();
            }
        });

        JFormattedTextField textField = ((JSpinner.DefaultEditor) runNum.getEditor()).getTextField();
        textField.setColumns(7); // 设置输入框的宽度为10个字符
        textField.setHorizontalAlignment(JTextField.CENTER); // 设置输入框内容居中
        textField.setToolTipText("Run num");

        isConcurrent.setToolTipText("Concurrent run");
    }


    private void componentLayout() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(toolBar, gbc);

        gbc.fill = GridBagConstraints.LINE_START;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.add(isConcurrent);
        panel.add(runNum);
        add(panel, gbc);

        gbc.fill = GridBagConstraints.LINE_START;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 1;
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel1.add(comboBox);
        panel1.add(saveParamButton);
        add(panel1, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 0;
        gbc.gridheight = -1;
        this.add(editor, gbc);
    }

    public JSONEditor getEditor() {
        return editor;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.IMPORT_EXPORT) {
            createImportExportDialog(((ImportExportEvent) event).getTitle()).show();
        }
    }

    private DialogWrapper createImportExportDialog(String title) {
        return new DialogWrapper(project, true, DialogWrapper.IdeModalityType.IDE) {
            {
                init();
            }

            @Override
            protected JComponent createCenterPanel() {
                ParamDataItem dataItem;
                if (Objects.equals(ImportExportPanel.EXPORT, title)) {
                    setTitle("Export");
                    dataItem = context.getSelectedItem();
                } else {
                    setTitle("Import");
                    dataItem = new ParamDataItem();
                }
                ImportExportPanel panel = new ImportExportPanel(project, title, dataItem);

                if (Objects.equals(ImportExportPanel.EXPORT, title)) {
                    setOKButtonText("Copy");
                    myOKAction = new DialogWrapperAction("Copy") {
                        @Override
                        protected void doAction(ActionEvent e) {
                            panel.copy();
                            NotifierUtil.notifyInfo(project, "Copy success");
                            dispose();
                        }
                    };
                } else {
                    myOKAction = new DialogWrapperAction("Import") {
                        @Override
                        protected void doAction(ActionEvent e) {
                            panel.importData();
                            context.fireEvent(EventHelper.createAddDataItemEvent(dataItem));
                            dispose();
                        }
                    };
                }
                return panel;
            }
        };
    }

    public Integer getRunNum() {
        return runNum.getNumber();
    }

    public Boolean getIsConcurrent() {
        return isConcurrent.isSelected();
    }

    public boolean isChangePid() {
        return !Objects.equals(toolBar.getPid(), toolBar.getInitPid());
    }

    public Integer getPid() {
        return toolBar.getPid();
    }
}
