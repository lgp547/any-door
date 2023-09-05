package io.github.lgp547.anydoorplugin.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.dialog.event.ComponentEvent;
import io.github.lgp547.anydoorplugin.dialog.event.DataEvent;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.DefaultMulticaster;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.event.Multicaster;
import io.github.lgp547.anydoorplugin.dialog.event.impl.AddDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.SelectItemChangedEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.UpdateDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.dialog.utils.IdeClassUtil;
import io.github.lgp547.anydoorplugin.util.JsonElementUtil;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 18:09
 **/
public class MethodDataContext implements Multicaster, Listener {

    private final Project project;
    private final ClassDataContext classDataContext;
    private final String qualifiedMethodName;
    private List<ParamDataItem> dataItems;
    private ParamDataItem selectedItem;
    private ParamDataItem freeItem;
    public final String cacheContent;

    private List<Listener> listeners = new ArrayList<>();

    public MethodDataContext(ClassDataContext classDataContext, String qualifiedMethodName, String cacheContent, Project project) {
        this(classDataContext, qualifiedMethodName, null, cacheContent, project);
    }

    public MethodDataContext(ClassDataContext classDataContext, String qualifiedMethodName, ParamDataItem selectedItem, String cacheContent, Project project) {
        this.project = project;
        this.cacheContent = cacheContent;
        this.classDataContext = classDataContext;
        this.qualifiedMethodName = qualifiedMethodName;
        this.dataItems = classDataContext.listMethodData(qualifiedMethodName);
        if (Objects.isNull(selectedItem)) {
            if (Objects.isNull(cacheContent)) {
                resetEmptyItem();
            } else {
                resetLastCacheParam();
            }
        } else {
            this.selectedItem = selectedItem;
        }
    }

    public ParamDataItem getSelectedItem() {
        return selectedItem;
    }

    public String getQualifiedMethodName() {
        return qualifiedMethodName;
    }

    public PsiClass getClazz() {
        return classDataContext.clazz;
    }


    public void sync() {
        dataItems = classDataContext.listMethodData(qualifiedMethodName);
        if (!Objects.equals(selectedItem, freeItem) && dataItems.stream().noneMatch(item -> Objects.equals(item, selectedItem))) {
            resetEmptyItemFillParam();
        }
    }

    @Override
    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        this.listeners.clear();
    }

    @Override
    public void fireEvent(Event event) {
        if (event instanceof DataEvent) {
            this.onEvent(event);
        } else if (event instanceof ComponentEvent) {
            for (Listener listener : listeners) {
                listener.onEvent(event);
            }
        } else {
            this.onEvent(event);
            for (Listener listener : listeners) {
                listener.onEvent(event);
            }
        }
    }

    public List<ParamDataItem> listDisplayData() {
        List<ParamDataItem> dataItemList = new ArrayList<>(dataItems);
        if (Objects.nonNull(freeItem)) {
            dataItemList.add(freeItem);
        }
        return dataItemList;
    }

    public PsiMethod getMethod() {
        String simpleMethodName = IdeClassUtil.getSimpleMethodName(qualifiedMethodName);
        if (Objects.nonNull(classDataContext.clazz)) {
            PsiMethod[] methods = classDataContext.clazz.findMethodsByName(simpleMethodName, false);
            for (PsiMethod method : methods) {
                if (Objects.equals(qualifiedMethodName, IdeClassUtil.getMethodQualifiedName(method))) {
                    return method;
                }
            }
        }
        return null;
    }

    public PsiParameterList getParamList() {
        PsiMethod method = getMethod();
        if (Objects.isNull(method)) {
            return null;
        }
        return method.getParameterList();
    }

    @Override
    public void onEvent(Event event) {
        if (Objects.equals(event.getType(), EventType.SELECT_ITEM_CHANGED)) {
            Long id = ((SelectItemChangedEvent) event).getId();
            findItem(id).ifPresent(this::selectItem);
            if (Objects.nonNull(id)) {
                clearEmptyItem();
            }
            fireDataChangeEvent(true);
            return;
        } else if (Objects.equals(event.getType(), EventType.ADD_DATA_ITEM)) {
            ParamDataItem dataItem = ((AddDataItemEvent) event).getDataItem();

            if (Objects.isNull(selectedItem.getId())) {
                selectedItem.setParam(dataItem.getParam());
                selectItem(selectedItem);
            } else {
                dataItem.setQualifiedName(qualifiedMethodName);
                addAndSelectItem(dataItem);
            }
        } else if (Objects.equals(event.getType(), EventType.REMOVE_DATA_ITEM)) {
//            Long id = ((RemoveDataItemEvent) event).getId();
//            removeItem(id);
        } else if (Objects.equals(event.getType(), EventType.UPDATE_ITEM_NAME)) {
            ParamDataItem dataItem = ((UpdateDataItemEvent) event).getDataItem();
            updateItemName(dataItem);
            flush();
        } else if (Objects.equals(event.getType(), EventType.ADD_SIMPLE_PARAM_ITEM)) {
            findItem(null)
                    .ifPresentOrElse(
                            item -> {
                                item.setParam(JsonElementUtil.getSimpleText(getParamList()));
                                selectItem(item);
                            },
                            this::resetEmptyItem);
        } else if (Objects.equals(event.getType(), EventType.ADD_ALL_PARAM_ITEM)) {
            findItem(null)
                    .ifPresentOrElse(
                            item -> {
                                item.setParam(JsonElementUtil.getJsonText(getParamList()));
                                selectItem(item);
                            },
                            this::resetEmptyItemFillParam);
        } else if (Objects.equals(event.getType(), EventType.ADD_CACHE_PARAM_ITEM)) {
            resetLastCacheParam();
        }
        fireDataChangeEvent(false);

    }

    private void fireDataChangeEvent(boolean selectItemChanged) {
        fireEvent(EventHelper.createDisplayDataChangeEvent(listDisplayData(), selectedItem, selectItemChanged));
    }

    private void resetEmptyItemFillParam() {
        resetEmptyItem();
        freeItem.setParam(JsonElementUtil.getJsonText(getParamList()));
    }

    private void resetEmptyItem() {
        freeItem = new ParamDataItem("", qualifiedMethodName, JsonElementUtil.getSimpleText(getParamList()));
        selectItem(freeItem);
    }

    private void resetLastCacheParam() {
        freeItem = new ParamDataItem("", qualifiedMethodName, cacheContent);
        selectItem(freeItem);
    }

    private Optional<ParamDataItem> findItem(Long id) {
        Predicate<ParamDataItem> predicate;
        if (Objects.isNull(id)) {
            predicate = item -> Objects.isNull(item.getId());
        } else {
            predicate = item -> Objects.equals(item.getId(), id);
        }

        return dataItems
                .stream()
                .filter(predicate)
                .findAny();
    }

    public void selectItem(ParamDataItem item) {
        this.selectedItem = item;
    }

//    public void removeItem(Long id) {
//        findItem(id).ifPresent(dataItems::remove);
//    }

    private void updateItemName(ParamDataItem dataItem) {
        findItem(dataItem.getId()).ifPresent(item -> item.setName(dataItem.getName()));
    }

    public void addAndSelectItem(ParamDataItem item) {
        dataItems.add(item);
        selectItem(item);
    }

    public void clearEmptyItem() {
        this.freeItem = null;
    }

    public void flush() {
        if (Objects.equals(selectedItem, freeItem)) {
            clearEmptyItem();
            ParamDataItem item = new ParamDataItem(selectedItem.getName(), selectedItem.getQualifiedName(), selectedItem.getParam());
            selectedItem = item;

            DefaultMulticaster.getInstance(project).fireEvent(EventHelper.createGlobalSaveDataChangeEvent(classDataContext.clazz.getQualifiedName(), qualifiedMethodName, List.of(item)));
        } else {
            DefaultMulticaster.getInstance(project).fireEvent(EventHelper.createGlobalUpdateDataChangeEvent(classDataContext.clazz.getQualifiedName(), qualifiedMethodName));
        }
    }
}
