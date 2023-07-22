package io.github.lgp547.anydoorplugin.dialog.event;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-19 19:29
 **/
public class Multicaster {

    protected final List<Listener> listeners;
    protected Listener dataListener;

    public Multicaster() {
        this(null);
    }

    public Multicaster(Listener listener) {
        this(listener, new ArrayList<>());
    }

    public Multicaster(Listener listener, List<Listener> listeners) {
        this.dataListener = listener;
        this.listeners = listeners;
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void fireEvent(Event event) {
        if (event instanceof ComponentEvent) {
            listeners.forEach(listener -> listener.onEvent(event));
        } else if (event instanceof DataEvent) {
            dataListener.onEvent(event);
        }
    }
}
