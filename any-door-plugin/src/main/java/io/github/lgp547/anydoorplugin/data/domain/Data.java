package io.github.lgp547.anydoorplugin.data.domain;

import java.util.ArrayList;
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
    }

    public Data(String identity) {
        this.identity = identity;
        this.timestamp = System.currentTimeMillis();
        this.dataList = new ArrayList<>();
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
        this.dataList = dataList;
    }
}
