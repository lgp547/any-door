package io.github.lgp547.anydoorplugin.dialog.event.impl;

import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-22 20:09
 **/
public class DataSyncEvent implements Event {

    private final String qualifiedMethodName;

    public DataSyncEvent(String qualifiedMethodName) {
        this.qualifiedMethodName = qualifiedMethodName;
    }

    public String getQualifiedMethodName() {
        return qualifiedMethodName;
    }

    @Override
    public EventType getType() {
        return EventType.DATA_SYNC;
    }
}
