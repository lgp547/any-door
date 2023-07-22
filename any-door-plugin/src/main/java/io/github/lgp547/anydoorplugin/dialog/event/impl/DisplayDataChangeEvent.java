package io.github.lgp547.anydoorplugin.dialog.event.impl;

import java.util.List;

import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.event.ComponentEvent;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 11:51
 **/
public class DisplayDataChangeEvent implements ComponentEvent {

    private List<ParamDataItem> displayList;
    private ParamDataItem selectedItem;

    public DisplayDataChangeEvent() {
    }

    public DisplayDataChangeEvent(List<ParamDataItem> displayList, ParamDataItem selectedItem) {
        this.displayList = displayList;
        this.selectedItem = selectedItem;
    }

    @Override
    public EventType getType() {
        return EventType.DISPLAY_DATA_CHANGE;
    }

    public List<ParamDataItem> getDisplayList() {
        return displayList;
    }

    public ParamDataItem getSelectedItem() {
        return selectedItem;
    }
}
