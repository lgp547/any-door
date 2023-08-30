package io.github.lgp547.anydoorplugin.dialog.event;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-22 19:07
 **/
@Service(value = Service.Level.PROJECT)
public final class DefaultMulticaster implements Multicaster {

//    public static final DefaultMulticaster INSTANCE = new DefaultMulticaster(project);

    private final Project project;
    private Listener dataChangeListener;
    private final List<Listener> listeners = new ArrayList<>();

    public static DefaultMulticaster getInstance(Project project) {
        return project.getService(DefaultMulticaster.class);
    }

    private DefaultMulticaster(Project project) {
        this.project = project;
    }

    public void setDataChangeListener(Listener dataChangeListener) {
        this.dataChangeListener = dataChangeListener;
    }

    @Override
    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        listeners.clear();
    }

    @Override
    public void fireEvent(Event event) {
        if (event instanceof DataEvent) {
            if (dataChangeListener != null) {
                dataChangeListener.onEvent(event);
            }
        } else {
            for (Listener listener : listeners) {
                listener.onEvent(event);
            }
        }
    }
}
