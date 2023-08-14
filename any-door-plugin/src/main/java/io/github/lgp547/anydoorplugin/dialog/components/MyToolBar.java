package io.github.lgp547.anydoorplugin.dialog.components;

import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.*;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBDimension;
import io.github.lgp547.anydoorplugin.dialog.event.Multicaster;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.util.AnyDoorIcons;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 11:08
 **/
public class MyToolBar extends CustomToolbar {

    private final Project project;
    private final Multicaster multicaster;

    public MyToolBar(Project project, Multicaster multicaster) {
        super();
        this.project = project;
        this.multicaster = multicaster;

        this.setFloatable(false);
        this.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, this.getBackground().darker()));

        initButtons();
    }

    private void initButtons() {
        this.addToolButton("Import", AnyDoorIcons.import_icon, AnyDoorIcons.import_hover_icon, e -> {
            multicaster.fireEvent(EventHelper.createImportExportEvent(ImportExportPanel.IMPORT));
        });

        this.addToolButton("Export", AnyDoorIcons.export_icon, AnyDoorIcons.export_icon, e -> {
            multicaster.fireEvent(EventHelper.createImportExportEvent(ImportExportPanel.EXPORT));
        });

        this.addToolButton("Last Call Param", AnyDoorIcons.last_call_icon, AnyDoorIcons.last_call_icon, e -> {

        });

        this.addToolButton("Example For All Param", AnyDoorIcons.example_icon, AnyDoorIcons.example_icon, e -> {
            multicaster.fireEvent(EventHelper.createAddAllParamItemEvent());
        });

        this.addToolButton("New Call Param", AnyDoorIcons.add_icon, AnyDoorIcons.add_hover_icon, e -> {
            multicaster.fireEvent(EventHelper.createAddSimpleParamItemEvent());
        });

    }

    @Override
    public JButton addToolButton(String tip, Icon icon, Icon hoverIcon, Consumer<ActionEvent> consumer) {
        JButton button = super.addToolButton(tip, icon, hoverIcon, consumer);
        button.setPreferredSize(new JBDimension(30, 30));
        return button;
    }

}
