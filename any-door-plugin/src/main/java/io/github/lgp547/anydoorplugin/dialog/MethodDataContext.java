package io.github.lgp547.anydoorplugin.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.data.utils.ParamIdentityHelper;
import io.github.lgp547.anydoorplugin.dialog.event.DataEvent;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.GlobalMulticaster;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.event.Multicaster;
import io.github.lgp547.anydoorplugin.dialog.event.impl.AddDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.SelectItemChangedEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.UpdateDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.util.JsonElementUtil;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 18:09
 **/
public class MethodDataContext implements Multicaster, Listener {

    private final PsiClass clazz;
    private final List<ParamDataItem> dataItems;
    private final String qualifiedMethodName;
    private ParamDataItem selectedItem;
    private ParamDataItem freeItem;

    private List<Listener> listeners = new ArrayList<>();

    public MethodDataContext(PsiClass clazz, List<ParamDataItem> dataItems, String qualifiedMethodName) {
        this(clazz, dataItems, qualifiedMethodName, null);
    }

    public MethodDataContext(PsiClass clazz, List<ParamDataItem> dataItems, String qualifiedMethodName, ParamDataItem selectedItem) {
        this.clazz = clazz;
        this.dataItems = dataItems;
        this.qualifiedMethodName = qualifiedMethodName;
        if (Objects.isNull(selectedItem)) {
            resetEmptyItem();
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
        return clazz;
    }

    public List<ParamDataItem> getDataItems() {
        return dataItems;
    }

    public void sync(List<ParamDataItem> items) {
        dataItems.clear();
        dataItems.addAll(items);

        boolean noneMatch = dataItems.stream().noneMatch(item -> Objects.equals(item.getId(), selectedItem.getId()));
        if (noneMatch) {
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
        } else {
            for (Listener listener : listeners) {
                listener.onEvent(event);
            }
        }
    }

    public List<ParamDataItem> listDisplayData() {
        List<ParamDataItem> dataItemList = dataItems
                .stream()
                .filter(item -> Objects.equals(item.getQualifiedName(), qualifiedMethodName))
                .collect(Collectors.toList());
        if (Objects.nonNull(freeItem)) {
            dataItemList.add(freeItem);
        }
        return dataItemList;
    }

    public PsiMethod getMethod() {
        String simpleMethodName = ParamIdentityHelper.getSimpleMethodName(qualifiedMethodName);
        if (Objects.nonNull(clazz)) {
            PsiMethod[] methods = clazz.findMethodsByName(simpleMethodName, false);
            for (PsiMethod method : methods) {
                if (Objects.equals(qualifiedMethodName, ParamIdentityHelper.getMethodQualifiedName(method))) {
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
        if (Objects.equals(event.getType(), EventType.ADD_DATA_ITEM)) {
            ParamDataItem dataItem = ((AddDataItemEvent) event).getDataItem();

            if (Objects.isNull(selectedItem.getId())) {
                selectedItem.setParam(dataItem.getParam());
                selectItem(selectedItem);
            } else {
                dataItem.setQualifiedName(qualifiedMethodName);
                addAndSelectItem(dataItem);
            }
        } else if (Objects.equals(event.getType(), EventType.SELECT_ITEM_CHANGED)) {
            Long id = ((SelectItemChangedEvent) event).getId();
            findItem(id).ifPresent(this::selectItem);
            if (Objects.nonNull(id)) {
                clearEmptyItem();
            }
        } else if (Objects.equals(event.getType(), EventType.REMOVE_DATA_ITEM)) {
//            Long id = ((RemoveDataItemEvent) event).getId();
//            removeItem(id);
        } else if (Objects.equals(event.getType(), EventType.UPDATE_ITEM_NAME)) {
            ParamDataItem dataItem = ((UpdateDataItemEvent) event).getDataItem();
            updateItemName(dataItem);
            GlobalMulticaster.INSTANCE.fireEvent(EventHelper.createGlobalDataChangeEvent(clazz.getQualifiedName(), qualifiedMethodName, dataItems));
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
        }

        fireEvent(EventHelper.createDisplayDataChangeEvent(listDisplayData(), selectedItem));
    }

    private void resetEmptyItemFillParam() {
        resetEmptyItem();
        freeItem.setParam(JsonElementUtil.getJsonText(getParamList()));
    }

    private void resetEmptyItem() {
        freeItem = new ParamDataItem("", qualifiedMethodName, JsonElementUtil.getSimpleText(getParamList()));
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

    public void removeItem(Long id) {
        findItem(id).ifPresent(dataItems::remove);
    }

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
}
