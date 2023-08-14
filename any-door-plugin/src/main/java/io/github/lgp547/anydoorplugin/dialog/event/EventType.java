package io.github.lgp547.anydoorplugin.dialog.event;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 10:23
 **/
public enum EventType {
    /**
     * 导入导出
     */
    IMPORT_EXPORT("IMPORT_EXPORT"),
    DISPLAY_DATA_CHANGE("DISPLAY_DATA_CHANGE"),
    ADD_DATA_ITEM("ADD_DATA_ITEM"),
    ADD_SIMPLE_PARAM_ITEM("ADD_SIMPLE_PARAM_ITEM"),
    ADD_ALL_PARAM_ITEM("ADD_ALL_PARAM_ITEM"),
    ADD_CACHE_PARAM_ITEM("ADD_CACHE_PARAM_ITEM"),
    REMOVE_DATA_ITEM("REMOVE_DATA_ITEM"),
    SELECT_ITEM_CHANGED("SELECT_ITEM_CHANGED"),
    UPDATE_DATA_ITEM("UPDATE_DATA_ITEM"),
    UPDATE_ITEM_NAME("UPDATE_ITEM_NAME"),
    GLOBAL_DATA_CHANGE("GLOBAL_DATA_CHANGE"),
    GLOBAL_SAVE_DATA_CHANGE("GLOBAL_SAVE_DATA_CHANGE"),
    GLOBAL_UPDATE_DATA_CHANGE("GLOBAL_UPDATE_DATA_CHANGE"),
    GLOBAL_DELETE_DATA_CHANGE("GLOBAL_DELETE_DATA_CHANGE"),
    DATA_SYNC("DATA_SYNC"),
    ;
    private String name;
    EventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
