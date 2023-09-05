package io.github.lgp547.anydoorplugin.dialog.event.impl;

import io.github.lgp547.anydoorplugin.dialog.event.ComponentEvent;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 10:25
 **/
public class ImportExportEvent implements ComponentEvent {

    private String title;

    public ImportExportEvent() {
    }

    public ImportExportEvent(String title) {
        this.title = title;
    }

    @Override
    public EventType getType() {
        return EventType.IMPORT_EXPORT;
    }

    public String getTitle() {
        return title;
    }
}
