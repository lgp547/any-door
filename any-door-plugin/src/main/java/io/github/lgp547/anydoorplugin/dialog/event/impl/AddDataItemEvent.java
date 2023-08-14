package io.github.lgp547.anydoorplugin.dialog.event.impl;

import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.event.DataEvent;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 19:45
 **/
public class AddDataItemEvent implements DataEvent {

    private final EventType type;
    private final ParamDataItem dataItem;

    public AddDataItemEvent() {
        this(EventType.ADD_DATA_ITEM, null);
    }

    public AddDataItemEvent(ParamDataItem dataItem) {
        this(EventType.ADD_DATA_ITEM, dataItem);
    }

    public AddDataItemEvent(EventType type, ParamDataItem dataItem) {
        this.type = type;
        this.dataItem = dataItem;
    }

    public ParamDataItem getDataItem() {
        return dataItem;
    }

    @Override
    public EventType getType() {
        return type;
    }
}
