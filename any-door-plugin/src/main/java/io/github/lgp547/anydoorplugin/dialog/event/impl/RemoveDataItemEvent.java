package io.github.lgp547.anydoorplugin.dialog.event.impl;

import io.github.lgp547.anydoorplugin.dialog.event.DataEvent;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 19:43
 **/
public class RemoveDataItemEvent implements DataEvent {

    private Long id;

    public RemoveDataItemEvent() {
    }

    public RemoveDataItemEvent(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public EventType getType() {
        return EventType.REMOVE_DATA_ITEM;
    }

}
