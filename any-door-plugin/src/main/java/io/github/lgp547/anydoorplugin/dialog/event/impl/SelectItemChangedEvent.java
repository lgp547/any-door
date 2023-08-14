package io.github.lgp547.anydoorplugin.dialog.event.impl;

import io.github.lgp547.anydoorplugin.dialog.event.DataEvent;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 20:06
 **/
public class SelectItemChangedEvent implements DataEvent {

    private Long id;

    public SelectItemChangedEvent() {
    }

    public SelectItemChangedEvent(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public EventType getType() {
        return EventType.SELECT_ITEM_CHANGED;
    }
}
