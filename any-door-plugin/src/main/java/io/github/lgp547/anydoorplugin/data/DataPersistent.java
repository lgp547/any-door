package io.github.lgp547.anydoorplugin.data;

import java.util.concurrent.Future;

import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.DataItem;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-28 20:00
 **/
public interface DataPersistent<T extends DataItem> {

    Future<?> saveAsync(Data<T> data);

    Data<T> load(String identity);

    Data<T> load(String identity, boolean shareable);
    Data<T> load(String identity, boolean shareable, boolean useCache);

}
