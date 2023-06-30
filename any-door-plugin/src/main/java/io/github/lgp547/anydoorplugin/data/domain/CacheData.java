package io.github.lgp547.anydoorplugin.data.domain;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-30 11:50
 **/
public class CacheData<T extends DataItem> {
    private String key;
    private String qualifiedName;
    private Data<T> data;

    public CacheData() {
    }

    public CacheData(String key, String qualifiedName, Data<T> data) {
        this.key = key;
        this.qualifiedName = qualifiedName;
        this.data = data;
    }

    public Data<T> data() {
        return data;
    }
}
