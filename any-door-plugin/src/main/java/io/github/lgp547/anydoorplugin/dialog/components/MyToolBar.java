package io.github.lgp547.anydoorplugin.dialog.components;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.panels.HorizontalBox;
import com.intellij.util.ui.JBDimension;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.Multicaster;
import io.github.lgp547.anydoorplugin.dialog.event.impl.AddDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;
import io.github.lgp547.anydoorplugin.util.AnyDoorIcons;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 11:08
 **/
public class MyToolBar extends CustomToolbar {

    private final Project project;
    private final Multicaster multicaster;
    private final JBIntSpinner pidSpinner;
    private Integer initPid;

    public MyToolBar(Project project, Multicaster multicaster) {
        super();
        this.project = project;
        this.multicaster = multicaster;

        this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, this.getBackground().darker()));

        initButtons();

        initPid = AnyDoorSettingsState.getAnyDoorSettingsState(project).pid.intValue();
        pidSpinner = new JBIntSpinner(initPid, -1, Integer.MAX_VALUE);
        JFormattedTextField textField = ((JSpinner.DefaultEditor) pidSpinner.getEditor()).getTextField();
        textField.setColumns(7); // 设置输入框的宽度为10个字符
        textField.setHorizontalAlignment(JTextField.CENTER); // 设置输入框内容居中
        textField.setToolTipText("Process id");
        super.add(new HorizontalBox());
        super.add(pidSpinner);


    }

    private void initButtons() {
        this.addToolButton("Import", AnyDoorIcons.import_icon, AnyDoorIcons.import_hover_icon, e -> {
            multicaster.fireEvent(EventHelper.createImportExportEvent(ImportExportPanel.IMPORT));
        });

        this.addToolButton("Export", AnyDoorIcons.export_icon, AnyDoorIcons.export_icon, e -> {
            multicaster.fireEvent(EventHelper.createImportExportEvent(ImportExportPanel.EXPORT));
        });

        this.addToolButton("New Call Param", AnyDoorIcons.add_icon, AnyDoorIcons.add_hover_icon, e -> {
            multicaster.fireEvent(EventHelper.createAddSimpleParamItemEvent());
        });

        this.addToolButton("Example For All Param", AnyDoorIcons.example_icon, AnyDoorIcons.example_icon, e -> {
            multicaster.fireEvent(EventHelper.createAddAllParamItemEvent());
        });

        this.addToolButton("Last Call Param", AnyDoorIcons.last_call_icon, AnyDoorIcons.last_call_icon, e -> {
            multicaster.fireEvent(new AddDataItemEvent(EventType.ADD_CACHE_PARAM_ITEM, null));
        });

        this.addToolButton("Pre run function", AnyDoorIcons.pre_run_icon, AnyDoorIcons.pre_run_icon, e -> {
            multicaster.fireEvent(() -> EventType.PRE_RUN_FUNCTION);
        });
    }

    @Override
    public JButton addToolButton(String tip, Icon icon, Icon hoverIcon, Consumer<ActionEvent> consumer) {
        JButton button = super.addToolButton(tip, icon, hoverIcon, consumer);
        button.setPreferredSize(new JBDimension(50, 30));
        return button;
    }

    public Integer getPid() {
        return pidSpinner.getNumber();
    }

    public Integer getInitPid() {
        return initPid;
    }
}
