package io.github.lgp547.anydoorplugin.dialog;

import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.components.MainPanel;
import io.github.lgp547.anydoorplugin.dialog.components.SaveDialog;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.GlobalMulticaster;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.event.impl.DataSyncEvent;
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
public class MainUI extends DialogWrapper implements Listener {

    private final Project project;

    private final MethodDataContext context;

    private MainPanel panel;

    private Consumer<String> okAction;

    public MainUI(@Nullable Project project, MethodDataContext context) {
        super(project, true, IdeModalityType.MODELESS);

        this.project = project;
        this.context = context;

        buttonSettings();

        GlobalMulticaster.INSTANCE.addListener(this);
        init();
    }

    private void buttonSettings() {
        setOKButtonText("RUN");
        setOKButtonIcon(AllIcons.Actions.RunAll);
    }

    public void setOkAction(Consumer<String> runnable) {
        this.okAction = runnable;
    }

    @Override
    protected void doOKAction() {
        if (Objects.nonNull(okAction)) {
            JSONEditor editor = panel.getEditor();
            String text = JsonUtil.compressJson(editor.getText());
            okAction.accept(text);
        }
        super.doOKAction();
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
            }
        };
        return new Action[]{getOKAction(), getCancelAction(), saveAction};
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        panel = new MainPanel(project, context);
        return panel;
    }

    @Override
    public void onEvent(Event event) {
        if (Objects.equals(EventType.DATA_SYNC, event.getType())) {
            DataSyncEvent syncEvent = (DataSyncEvent) event;
            if (Objects.equals(syncEvent.getQualifiedMethodName(), context.getQualifiedMethodName())) {
                context.sync();
                context.fireEvent(EventHelper.createDisplayDataChangeEvent(context.listDisplayData(), context.getSelectedItem()));
            }
        }
    }

    @Override
    protected void dispose() {
        GlobalMulticaster.INSTANCE.removeListener(this);
        super.dispose();
    }
}
