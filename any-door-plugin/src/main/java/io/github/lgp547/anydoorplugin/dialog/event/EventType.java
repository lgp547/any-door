package io.github.lgp547.anydoorplugin.dialog.event;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-20 10:23
 **/
public enum EventType {
    IMPORT_EXPORT("IMPORT_EXPORT"),
    IMPORT_COMPLETED("IMPORT_COMPLETED"),
    DISPLAY_DATA_CHANGE("DISPLAY_DATA_CHANGE"),
    ADD_DATA_ITEM("ADD_DATA_ITEM"),
    ADD_SIMPLE_PARAM_ITEM("ADD_SIMPLE_PARAM_ITEM"),
    ADD_ALL_PARAM_ITEM("ADD_ALL_PARAM_ITEM"),
    REMOVE_DATA_ITEM("REMOVE_DATA_ITEM"),
    SELECT_ITEM_CHANGED("SELECT_ITEM_CHANGED"),
    UPDATE_DATA_ITEM("UPDATE_DATA_ITEM"),
    UPDATE_ITEM_NAME("UPDATE_ITEM_NAME"),
    ;
    private String name;
    EventType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
