package io.github.lgp547.anydoorplugin.data.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.lgp547.anydoor.util.JsonUtil;

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

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Data<T> getData() {
        return data;
    }

    public void setData(Data<T> data) {
        this.data = data;
    }

    public Data<T> data() {
        return data;
    }

    public Data<T> cloneData() {
        try {
            String json = JsonUtil.objectMapper.writeValueAsString(this.data);
            return JsonUtil.toJavaBean(json, Data.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
