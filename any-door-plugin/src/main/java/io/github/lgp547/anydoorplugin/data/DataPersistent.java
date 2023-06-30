package io.github.lgp547.anydoorplugin.data;

import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.DataItem;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-28 20:00
 **/
public interface DataPersistent<T extends DataItem> {

    Data<T> load(Project project, String identity);

}
