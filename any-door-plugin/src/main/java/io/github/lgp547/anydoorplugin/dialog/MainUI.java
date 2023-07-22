package io.github.lgp547.anydoorplugin.dialog;

import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.components.MainPanel;
import io.github.lgp547.anydoorplugin.dialog.components.SaveDialog;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-07 11:35
 **/
public class MainUI extends DialogWrapper {

    private final Project project;

    private final DataContext context;

    private MainPanel panel;

    public MainUI(@Nullable Project project, DataContext context) {
        super(project, true, IdeModalityType.MODELESS);

        this.project = project;
        this.context = context;

        buttonSettings();
        init();
    }

    private void buttonSettings() {
        setOKButtonText("RUN");
        setOKButtonIcon(AllIcons.Actions.RunAll);
    }

    @Override
    protected Action @NotNull [] createActions() {
        DialogWrapperAction saveAction = new DialogWrapperAction("Save") {
            @Override
            protected void doAction(ActionEvent e) {
                if (Objects.isNull(context.getClazz())) {
                    throw new RuntimeException("Current Class Not Exist");
                }

                JSONEditor editor = panel.getEditor();
                String text = JsonUtil.compressJson(editor.getText());

                ParamDataItem selectedDataItem = context.getSelectedDataItem();
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

                context.fireEvent(EventHelper.createDisplayDataChangeEvent(context.listDisplayData(), context.getSelectedDataItem()));
            }
        };
        return new Action[]{getOKAction(), getCancelAction(), saveAction};
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        panel = new MainPanel(project, context);
        return panel;
    }

}
