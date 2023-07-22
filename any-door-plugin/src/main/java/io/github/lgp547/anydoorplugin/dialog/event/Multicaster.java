package io.github.lgp547.anydoorplugin.dialog.event;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-19 19:29
 **/
public interface Multicaster {
    void addListener(Listener listener);

    void removeListener(Listener listener);

    void removeAllListeners();

    void fireEvent(Event event);
}
