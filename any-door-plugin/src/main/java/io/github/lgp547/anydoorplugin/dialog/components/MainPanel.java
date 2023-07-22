package io.github.lgp547.anydoorplugin.dialog.components;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBDimension;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.DataContext;
import io.github.lgp547.anydoorplugin.dialog.JSONEditor;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.impl.ImportExportEvent;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.util.NotifierUtil;

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
    private final DataContext context;

    public MainPanel(Project project, DataContext context) {
        super(new GridBagLayout());
        setPreferredSize(new JBDimension(670, 500));

        this.project = project;
        this.context = context;

        toolBar = new MyToolBar(project, context);
        editor = new MyEditor(context, "{}", context.getParamList(), project);
        comboBox = new MyComboBox(project, context);

        this.context.addListener(this);
        this.context.addListener(comboBox);
        this.context.addListener(editor);

        context.fireEvent(EventHelper.createDisplayDataChangeEvent(context.listDisplayData(), context.getSelectedDataItem()));

        componentLayout();
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
        add(comboBox, gbc);

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
                    dataItem = context.getSelectedDataItem();
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
}
