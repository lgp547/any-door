package io.github.lgp547.anydoorplugin.dialog.event.impl;

import java.util.List;

import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.event.DataEvent;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-22 19:52
 **/
public class GlobalDataChangeEvent implements DataEvent {

    private final EventType type;
    private final String qualifiedClassName;
    private final String qualifiedMethodName;
    private final List<ParamDataItem> dataItems;

    public GlobalDataChangeEvent(String qualifiedClassName, String qualifiedMethodName, List<ParamDataItem> dataItems) {
        this(qualifiedClassName, qualifiedMethodName, dataItems, EventType.GLOBAL_DATA_CHANGE);
    }

    public GlobalDataChangeEvent(String qualifiedClassName, String qualifiedMethodName, List<ParamDataItem> dataItems, EventType type) {
        this.qualifiedClassName = qualifiedClassName;
        this.qualifiedMethodName = qualifiedMethodName;
        this.dataItems = dataItems;
        this.type = type;
    }


    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public String getQualifiedMethodName() {
        return qualifiedMethodName;
    }

    public List<ParamDataItem> getDataItems() {
        return dataItems;
    }

    @Override
    public EventType getType() {
        return type;
    }
}
