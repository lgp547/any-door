package io.github.lgp547.anydoorplugin.dialog;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import io.github.lgp547.anydoorplugin.data.DataService;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.data.utils.ParamIdentityHelper;
import io.github.lgp547.anydoorplugin.dialog.event.Event;
import io.github.lgp547.anydoorplugin.dialog.event.EventType;
import io.github.lgp547.anydoorplugin.dialog.event.Listener;
import io.github.lgp547.anydoorplugin.dialog.event.Multicaster;
import io.github.lgp547.anydoorplugin.dialog.event.impl.AddDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.RemoveDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.SelectItemChangedEvent;
import io.github.lgp547.anydoorplugin.dialog.event.impl.UpdateDataItemEvent;
import io.github.lgp547.anydoorplugin.dialog.utils.EventHelper;
import io.github.lgp547.anydoorplugin.util.JsonElementUtil;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 18:09
 **/
public class DataContext extends Multicaster implements Listener {

    private final DataService<ParamDataItem> dataService;
    private final PsiClass clazz;
    private final String qualifiedMethodName;
    private final Data<ParamDataItem> data;
    private ParamDataItem select;

    public DataContext(DataService<ParamDataItem> dataService, PsiClass clazz, String qualifiedMethodName, Data<ParamDataItem> data, ParamDataItem select) {
        super();
        this.dataListener = this;

        this.dataService = dataService;
        this.clazz = clazz;
        this.qualifiedMethodName = qualifiedMethodName;
        this.data = data;
        this.select = select;
    }

    public DataContext(DataService<ParamDataItem> dataService, PsiClass clazz, String qualifiedMethodName, Data<ParamDataItem> data) {
        this(dataService, clazz, qualifiedMethodName, data, null);
        createAndSelectItem(null, JsonElementUtil.getJsonText(getParamList()));
    }

    public List<ParamDataItem> listDisplayData() {
        return data.getDataList()
                .stream()
                .filter(item -> Objects.equals(item.getQualifiedName(), qualifiedMethodName))
                .collect(Collectors.toList());
    }

    public ParamDataItem getSelectedDataItem() {
        return select;
    }

    public ParamDataItem createAndSelectItem(String name, String param) {
        PsiMethod method = getMethod();
        if (Objects.isNull(method)) {
            throw new RuntimeException("method not found");
        }
        ParamDataItem item = new ParamDataItem(name, qualifiedMethodName, param);
        data.getDataList().add(item);
        selectItem(item);
        return item;
    }

    public PsiClass getClazz() {
        return clazz;
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

    public void flush() {
        dataService.save(data);
    }

    public void removeItem(Long id) {
        findItem(id).ifPresent(item -> data.getDataList().remove(item));
    }

    public void addAndSelectItem(ParamDataItem item) {
        data.getDataList().add(item);
        selectItem(item);
    }

    public void selectItem(ParamDataItem item) {
        this.select = item;
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

            if (Objects.isNull(select.getId())) {
                select.setParam(dataItem.getParam());
                selectItem(select);
            } else {
                addAndSelectItem(dataItem);
            }
        } else if (Objects.equals(event.getType(), EventType.SELECT_ITEM_CHANGED)) {
            Long id = ((SelectItemChangedEvent) event).getId();
            findItem(id).ifPresent(this::selectItem);
            if (Objects.nonNull(id)) {
                removeItem(null);
            }
        } else if (Objects.equals(event.getType(), EventType.REMOVE_DATA_ITEM)) {
            Long id = ((RemoveDataItemEvent) event).getId();
            removeItem(id);
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
                            () -> createAndSelectItem(null, JsonElementUtil.getSimpleText(getParamList())));
        } else if (Objects.equals(event.getType(), EventType.ADD_ALL_PARAM_ITEM)) {
            findItem(null)
                    .ifPresentOrElse(
                            item -> {
                                item.setParam(JsonElementUtil.getJsonText(getParamList()));
                                selectItem(item);
                            },
                            () -> createAndSelectItem(null, JsonElementUtil.getJsonText(getParamList())));
        }

        fireEvent(EventHelper.createDisplayDataChangeEvent(listDisplayData(), select));
    }

    private void updateItemName(ParamDataItem dataItem) {
        findItem(dataItem.getId()).ifPresent(item -> item.setName(dataItem.getName()));
    }

    private Optional<ParamDataItem> findItem(Long id) {
        Predicate<ParamDataItem> predicate;
        if (Objects.isNull(id)) {
            predicate = item -> Objects.isNull(item.getId());
        } else {
            predicate = item -> Objects.equals(item.getId(), id);
        }

        return data.getDataList()
                .stream()
                .filter(predicate)
                .findAny();
    }
}
