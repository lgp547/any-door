package io.github.lgp547.anydoorplugin.data.impl;

import java.util.Objects;
import java.util.concurrent.Future;

import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.AbstractParamDataReaderWriter;
import io.github.lgp547.anydoorplugin.data.IdGenerator;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamIndexData;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-29 11:34
 **/
public class ParamIndexReaderWriter extends AbstractParamDataReaderWriter<ParamIndexData> {

    private static final String INDEX_SUFFIX = "&index";

    public ParamIndexReaderWriter(Project project) {
        this(project, new DefaultIdGenerator());
    }

    public ParamIndexReaderWriter(Project project, IdGenerator idGenerator) {
        super(project, idGenerator);
        Objects.requireNonNull(project);
    }

    @Override
    protected Data<ParamIndexData> read(String identity, boolean shareable, boolean useCache) {
        Objects.requireNonNull(identity);

        String key = genKey(identity, shareable);
        String filePath = genFilePath(key);

        if (useCache) {
            return readAndCache(key, filePath).cloneData();
        }

        return doRead(key, filePath);
    }


    @Override
    protected Future<?> write(Data<ParamIndexData> data) {

        Objects.requireNonNull(data);
        Objects.requireNonNull(data.getIdentity());

        data.setTimestamp(System.currentTimeMillis());

        return doWrite(data.getIdentity(), genFilePath(data.getIdentity()), data);
    }


    @Override
    protected String genFilePath(String key) {
        Objects.requireNonNull(key);

        String filepath = key + ".json";

        return project.getBasePath() + DATA_BASE_DIR + FILE_SEPARATOR + filepath;
    }

    @Override
    protected String genKey(String identity, boolean shareable) {
        return identity + (shareable ? INDEX_SUFFIX + SHAREABLE_SUFFIX : INDEX_SUFFIX);
    }
}
