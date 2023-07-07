package io.github.lgp547.anydoorplugin.data.impl;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.DataService;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-05 19:43
 **/
@Service
public final class ParamDataService implements DataService<ParamDataItem> {
    private final Project project;
    private final ParamDataReaderWriter paramDataReaderWriter;

    public Project getProject() {
        return project;
    }

    public ParamDataService(Project project) {
        this.project = project;
        this.paramDataReaderWriter = new ParamDataReaderWriter(project);
    }

    @Override
    public Data<ParamDataItem> read(String identity) {
        return paramDataReaderWriter.load(identity);
    }
}
