package io.github.lgp547.anydoorplugin.data;

import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.DataItem;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-28 19:43
 **/
public interface DataService<T extends DataItem> {

    Data<T> read(String identity);

}
