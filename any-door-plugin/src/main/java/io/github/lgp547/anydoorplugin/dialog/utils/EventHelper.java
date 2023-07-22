package io.github.lgp547.anydoorplugin.dialog.utils;

import java.util.List;

import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.impl.AddDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.DataSyncEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.DisplayDataChangeEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.GlobalDataChangeEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.ImportExportEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.SelectItemChangedEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.UpdateDataItemEvent;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-19 20:06
 **/
public class EventHelper {

    public static Event createImportExportEvent(String type) {
        return new ImportExportEvent(type);
    }

    public static Event createAddDataItemEvent(ParamDataItem dataItem) {
        return new AddDataItemEvent(dataItem);
    }

    public static Event createAddSimpleParamItemEvent() {
        return new AddDataItemEvent(EventType.ADD_SIMPLE_PARAM_ITEM, null);
    }

    public static Event createAddAllParamItemEvent() {
        return new AddDataItemEvent(EventType.ADD_ALL_PARAM_ITEM, null);
    }

    public static Event createUpdateDataItemEvent(ParamDataItem dataItem) {
        return new UpdateDataItemEvent(dataItem);
    }

    public static Event createUpdateItemNameEvent(ParamDataItem dataItem) {
        return new UpdateDataItemEvent(EventType.UPDATE_ITEM_NAME, dataItem);
    }

    public static Event createSelectItemChangedEvent(Long id) {
        return new SelectItemChangedEvent(id);
    }

    public static Event createDisplayDataChangeEvent(List<ParamDataItem> displayList, ParamDataItem selectedItem) {
        return new DisplayDataChangeEvent(displayList, selectedItem);
    }

    public static Event createGlobalDataChangeEvent(String qualifiedClassName, String qualifiedMethodName, List<ParamDataItem> dataItems) {
        return new GlobalDataChangeEvent(qualifiedClassName, qualifiedMethodName, dataItems);
    }

    public static Event createDataSyncEvent(String qualifiedMethodName, List<ParamDataItem> dataItems) {
        return new DataSyncEvent(qualifiedMethodName, dataItems);
    }
}
