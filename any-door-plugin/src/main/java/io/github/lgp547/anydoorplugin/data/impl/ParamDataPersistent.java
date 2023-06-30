package io.github.lgp547.anydoorplugin.data.impl;

import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.DataPersistent;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-30 10:33
 **/
public class ParamDataPersistent implements DataPersistent<ParamDataItem> {
    private ParamDataReaderWriter dataReaderWriter;

    @Override
    public Data<ParamDataItem> load(Project project, String identity) {
        return dataReaderWriter.readClassParamData(project, identity);
    }
}
