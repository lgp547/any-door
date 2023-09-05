package io.github.lgp547.anydoorplugin.data.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-28 19:47
 **/
public class Data<T extends DataItem> {

    protected String identity;
    protected long timestamp;
    protected List<T> dataList;

    public Data() {
        this.dataList = Collections.synchronizedList(new ArrayList<>());
    }

    public Data(String identity) {
        this();
        this.identity = identity;
        this.timestamp = System.currentTimeMillis();
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList.clear();
        this.dataList.addAll(dataList);
    }

    public void removeItems(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<T> dataList = getDataList();
        if (dataList == null || dataList.isEmpty()) {
            return;
        }

        List<T> removeList = new ArrayList<>();
        for (T dataItem : dataList) {
            if (ids.contains(dataItem.getId())) {
                removeList.add(dataItem);
            }
        }

        dataList.removeAll(removeList);
    }
}
