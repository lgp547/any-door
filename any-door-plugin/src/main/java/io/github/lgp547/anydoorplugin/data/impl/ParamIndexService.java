package io.github.lgp547.anydoorplugin.data.impl;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.DataService;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamIndexData;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-22 12:28
 **/
@Service
public final class ParamIndexService implements DataService<ParamIndexData> {
    private final Project project;
    private final ParamIndexReaderWriter paramIndexReaderWriter;

    public Project getProject() {
        return project;
    }

    public ParamIndexService(Project project) {
        this.project = project;

        this.paramIndexReaderWriter = new ParamIndexReaderWriter(project);
    }

    @Override
    public Data<ParamIndexData> find(String identity) {
        return paramIndexReaderWriter.load(identity);
    }

    @Override
    public Data<ParamIndexData> findNoCache(String identity) {
        return paramIndexReaderWriter.load(identity, false, false);
    }

    @Override
    public void save(Data<ParamIndexData> data) {

        paramIndexReaderWriter.saveAsync(data);
    }
}
