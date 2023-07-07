package io.github.lgp547.anydoorplugin.data.domain;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-30 11:50
 **/
public class CacheData<T extends DataItem> {

    private String identity;
    private Data<T> data;

    public CacheData() {
    }

    public CacheData(String identity, Data<T> data) {
        this.identity = identity;
        this.data = data;
    }

    public Data<T> data() {
        return data;
    }
}
