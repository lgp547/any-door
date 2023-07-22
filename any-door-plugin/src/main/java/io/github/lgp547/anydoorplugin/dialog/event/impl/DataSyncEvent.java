package io.github.lgp547.anydoorplugin.dialog.event.impl;

import java.util.List;

import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-22 20:09
 **/
public class DataSyncEvent implements Event {

    private final String qualifiedMethodName;
    private final List<ParamDataItem> dataItems;

    public DataSyncEvent(String qualifiedMethodName, List<ParamDataItem> dataItems) {
        this.qualifiedMethodName = qualifiedMethodName;
        this.dataItems = dataItems;
    }

    public String getQualifiedMethodName() {
        return qualifiedMethodName;
    }

    public List<ParamDataItem> getDataItems() {
        return dataItems;
    }

    @Override
    public EventType getType() {
        return EventType.DATA_SYNC;
    }
}
