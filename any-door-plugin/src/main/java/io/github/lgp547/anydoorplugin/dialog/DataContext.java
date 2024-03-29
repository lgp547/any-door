package io.github.lgp547.anydoorplugin.dialog;

import com.intellij.openapi.components.Service;
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
import io.github.lgp547.anydoorplugin.dialog.event.DefaultMulticaster;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.event.impl.GlobalDataChangeEvent;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.dialog.utils.IdeClassUtil;
import io.github.lgp547.anydoorplugin.dto.ParamCacheDto;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 18:09
 **/
@Service(value = Service.Level.PROJECT)
public final class DataContext implements Listener {

    private final Project project;
    private final DataService<ParamIndexData> indexService;
    private final DataService<ParamDataItem> dataService;


    private final Map<String, ClassDataContext> contextMap;
    private final Data<ParamIndexData> indexData;

    public static DataContext instance(Project project) {
        return project.getService(DataContext.class);
    }

    public DataContext(Project project) {
        this.project = project;
        this.indexService = project.getService(ParamIndexService.class);
        this.dataService = project.getService(ParamDataService.class);
        this.contextMap = new ConcurrentHashMap<>();
        DefaultMulticaster.getInstance(project).setDataChangeListener(this);

        indexData = indexService.find(project.getName());
    }

    public ClassDataContext getClassDataContext(String qualifiedClassName) {
        Objects.requireNonNull(qualifiedClassName);

        ClassDataContext classDataContext = contextMap.get(qualifiedClassName);
        if (classDataContext == null || classDataContext.clazz == null || !classDataContext.clazz.isValid()) {
            Data<ParamDataItem> data = dataService.find(qualifiedClassName);
            PsiClass psiClass = IdeClassUtil.findClass(project, qualifiedClassName);
            return new ClassDataContext(psiClass, data, project);
        }
        return classDataContext;
    }


    public ClassDataContext getClassDataContextNoCache(String qualifiedClassName) {
        Data<ParamDataItem> data = dataService.findNoCache(qualifiedClassName);
        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(qualifiedClassName, GlobalSearchScope.allScope(project));
        ClassDataContext context = new ClassDataContext(psiClass, data, project);
        contextMap.put(qualifiedClassName, context);
        return context;
    }

//    public MethodDataContext getExecuteDataContext(String qualifiedClassName, String qualifiedMethodName, ParamCacheDto cache) {
//        return getExecuteDataContext(qualifiedClassName, qualifiedMethodName, null, cache.content());
//    }

    public MethodDataContext getExecuteDataContext(String qualifiedClassName, String qualifiedMethodName, Long selectedId, String cacheContent) {
        Objects.requireNonNull(qualifiedClassName);
        Objects.requireNonNull(qualifiedMethodName);

        ClassDataContext classDataContext = getClassDataContext(qualifiedClassName);

        return classDataContext.newMethodDataContext(qualifiedMethodName, selectedId, cacheContent);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof GlobalDataChangeEvent) {
            GlobalDataChangeEvent changeEvent = (GlobalDataChangeEvent) event;

            if (Objects.equals(event.getType(), EventType.GLOBAL_SAVE_DATA_CHANGE)) {
                contextMap.computeIfPresent(changeEvent.getQualifiedClassName(), (k, v) -> {
                    v.addItems(changeEvent.getDataItems());
                    dataService.save(v.data);

                    List<ParamIndexData> dataList = changeEvent.getDataItems().stream().map(ParamDataItem::toIndexData).collect(Collectors.toList());
                    indexData.getDataList().addAll(dataList);
                    indexService.save(indexData);
                    return v;
                });


            } else if (Objects.equals(event.getType(), EventType.GLOBAL_DELETE_DATA_CHANGE)) {
                contextMap.computeIfPresent(changeEvent.getQualifiedClassName(), (k, v) -> {
                    v.removeItems(changeEvent.getDataItems());
                    dataService.save(v.data);

                    Set<Long> idSet = changeEvent.getDataItems().stream().map(ParamDataItem::getId).collect(Collectors.toSet());
                    indexData.setDataList(indexData.getDataList().stream().filter(item -> !idSet.contains(item.getId())).collect(Collectors.toList()));
                    indexService.save(indexData);
                    return v;
                });

            } else if (Objects.equals(event.getType(), EventType.GLOBAL_UPDATE_DATA_CHANGE)) {
                contextMap.computeIfPresent(changeEvent.getQualifiedClassName(), (k, v) -> {

                    List<ParamIndexData> dataList = v.data.getDataList().stream().map(ParamDataItem::toIndexData).collect(Collectors.toList());
                    indexData.getDataList().removeIf(item -> Objects.equals(item.getQualifiedMethodName(), changeEvent.getQualifiedMethodName()));

                    indexData.getDataList().addAll(dataList);
                    indexService.save(indexData);
                    return v;
                });
            }
            DefaultMulticaster.getInstance(project).fireEvent(EventHelper.createDataSyncEvent(changeEvent.getQualifiedMethodName()));
        }
    }

    public List<ParamIndexData> search(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        List<ParamIndexData> dataList = indexData.getDataList().stream()
                .filter(item -> item.getName().toLowerCase().contains(text.toLowerCase())
                        || item.getQualifiedMethodName().toLowerCase().contains(text.toLowerCase()))
                .collect(Collectors.toList());
        return dataList;
    }
}
