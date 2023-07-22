package io.github.lgp547.anydoorplugin.dialog;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import io.github.lgp547.anydoorplugin.data.DataService;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.data.domain.ParamIndexData;
import io.github.lgp547.anydoorplugin.data.impl.ParamDataService;
import io.github.lgp547.anydoorplugin.data.impl.ParamIndexService;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.GlobalMulticaster;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.event.impl.GlobalDataChangeEvent;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 18:09
 **/
public class DataContext implements Listener {

    private static DataContext instance;
    private Project project;
    private DataService<ParamIndexData> indexService;
    private DataService<ParamDataItem> dataService;


    private Map<String, ClassDataContext> contextMap;

    public static DataContext instance(Project project) {
        if (instance == null) {
            synchronized (DataContext.class) {
                if (instance == null) {
                    instance = new DataContext(project);
                }
            }
        }
        return instance;
    }

    public DataContext(Project project) {
        this.project = project;
        this.indexService = project.getService(ParamIndexService.class);
        this.dataService = project.getService(ParamDataService.class);
        this.contextMap = new ConcurrentHashMap<>();
        GlobalMulticaster.INSTANCE.setDataChangeListener(this);
    }

    public MethodDataContext getExecuteDataContext(String qualifiedClassName, String qualifiedMethodName) {
        return getExecuteDataContext(qualifiedClassName, qualifiedMethodName, null);
    }

    public MethodDataContext getExecuteDataContext(String qualifiedClassName, String qualifiedMethodName, Long selectedId) {
        Objects.requireNonNull(qualifiedClassName);
        Objects.requireNonNull(qualifiedMethodName);

        contextMap.computeIfAbsent(qualifiedClassName, k -> {
            Data<ParamDataItem> data = dataService.find(qualifiedClassName);
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedClassName, GlobalSearchScope.allScope(project));
            return new ClassDataContext(psiClass, data);
        });

        ClassDataContext classDataContext = contextMap.get(qualifiedClassName);

        return classDataContext.newMethodDataContext(qualifiedMethodName, selectedId);
    }

    @Override
    public void onEvent(Event event) {
        if (Objects.equals(event.getType(), EventType.GLOBAL_DATA_CHANGE)) {
            GlobalDataChangeEvent changeEvent = (GlobalDataChangeEvent) event;
            ClassDataContext context = contextMap.computeIfPresent(changeEvent.getQualifiedClassName(), (k, v) -> {
                v.updateItems(changeEvent.getQualifiedMethodName(), changeEvent.getDataItems());
                return v;
            });

            if (Objects.nonNull(context)) {
                GlobalMulticaster.INSTANCE.fireEvent(EventHelper.createDataSyncEvent(changeEvent.getQualifiedMethodName(), changeEvent.getDataItems()));
            }
        }
    }
}
