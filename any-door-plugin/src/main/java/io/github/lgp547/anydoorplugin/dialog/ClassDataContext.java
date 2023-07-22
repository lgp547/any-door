package io.github.lgp547.anydoorplugin.dialog;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.intellij.psi.PsiClass;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-18 18:09
 **/
public class ClassDataContext {

    protected final PsiClass clazz;
    protected final Data<ParamDataItem> data;


    public ClassDataContext(PsiClass clazz, Data<ParamDataItem> data) {
        this.clazz = clazz;
        this.data = data;
    }

    public void updateItems(String qualifiedMethodName, List<ParamDataItem> items) {
        List<ParamDataItem> itemList = data.getDataList()
                .stream()
                .filter(item -> !Objects.equals(item.getQualifiedName(), qualifiedMethodName))
                .collect(Collectors.toList());
        itemList.addAll(items);
        data.setDataList(itemList);
    }

    public PsiClass getClazz() {
        return clazz;
    }

    public MethodDataContext newMethodDataContext(String qualifiedMethodName, Long selectedId) {
        List<ParamDataItem> dataItems = data.getDataList()
                .stream()
                .filter(item -> Objects.equals(item.getQualifiedName(), qualifiedMethodName))
                .collect(Collectors.toList());
        if (selectedId == null) {
            return new MethodDataContext(clazz, dataItems, qualifiedMethodName);
        } else {
            ParamDataItem dataItem = dataItems.stream().filter(item -> Objects.equals(item.getId(), selectedId)).findAny().orElse(null);
            return new MethodDataContext(clazz, dataItems, qualifiedMethodName, dataItem);
        }
    }

//    public PsiMethod getMethod() {
//        String simpleMethodName = ParamIdentityHelper.getSimpleMethodName(qualifiedMethodName);
//        if (Objects.nonNull(clazz)) {
//            PsiMethod[] methods = clazz.findMethodsByName(simpleMethodName, false);
//            for (PsiMethod method : methods) {
//                if (Objects.equals(qualifiedMethodName, ParamIdentityHelper.getMethodQualifiedName(method))) {
//                    return method;
//                }
//            }
//        }
//        return null;
//    }
//
//    public void removeItem(Long id) {
//        findItem(id).ifPresent(item -> data.getDataList().remove(item));
//    }
//
//
//    public PsiParameterList getParamList() {
//        PsiMethod method = getMethod();
//        if (Objects.isNull(method)) {
//            return null;
//        }
//        return method.getParameterList();
//    }
//
//    private void updateItemName(ParamDataItem dataItem) {
//        findItem(dataItem.getId()).ifPresent(item -> item.setName(dataItem.getName()));
//    }
//
//    private Optional<ParamDataItem> findItem(Long id) {
//        Predicate<ParamDataItem> predicate;
//        if (Objects.isNull(id)) {
//            predicate = item -> Objects.isNull(item.getId());
//        } else {
//            predicate = item -> Objects.equals(item.getId(), id);
//        }
//
//        return data.getDataList()
//                .stream()
//                .filter(predicate)
//                .findAny();
//    }
}
