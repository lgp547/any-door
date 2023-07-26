package io.github.lgp547.anydoorplugin.dialog.event.impl;

import java.util.Collection;
import java.util.Set;

import io.github.lgp547.anydoorplugin.dialog.event.DataEvent;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 19:43
 **/
public class RemoveDataItemEvent implements DataEvent {

    private Set<Long> itemIds;

    public RemoveDataItemEvent() {
    }

    public RemoveDataItemEvent(Collection<Long> itemIds) {
        this.itemIds = Set.copyOf(itemIds);
    }

    public Set<Long> getItemIds() {
        return itemIds;
    }

    @Override
    public EventType getType() {
        return EventType.REMOVE_DATA_ITEM;
    }

}
