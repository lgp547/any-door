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

    protected List<T> dataList;

    public Data() {
    }

    public Data(String identity) {
        this.identity = identity;
        this.dataList = new ArrayList<>();
    }
}
