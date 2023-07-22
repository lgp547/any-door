package io.github.lgp547.anydoorplugin.dialog.event.impl;

import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.event.DataEvent;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-21 10:26
 **/
public class UpdateDataItemEvent implements DataEvent {

    private final EventType type;
    private ParamDataItem dataItem;

    public UpdateDataItemEvent() {
        this(EventType.UPDATE_DATA_ITEM, null);
    }

    public UpdateDataItemEvent(ParamDataItem dataItem) {
        this(EventType.UPDATE_DATA_ITEM, dataItem);
    }

    public UpdateDataItemEvent(EventType type, ParamDataItem dataItem) {
        this.type = type;
        this.dataItem = dataItem;
    }

    public ParamDataItem getDataItem() {
        return dataItem;
    }

    @Override
    public EventType getType() {
        return EventType.UPDATE_DATA_ITEM;
    }
}
