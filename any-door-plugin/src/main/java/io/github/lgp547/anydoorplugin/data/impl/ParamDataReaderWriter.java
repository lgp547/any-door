package io.github.lgp547.anydoorplugin.data.impl;

import java.util.Objects;
import java.util.concurrent.Future;

import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.AbstractParamDataReaderWriter;
import io.github.lgp547.anydoorplugin.data.IdGenerator;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-29 11:34
 **/
public class ParamDataReaderWriter extends AbstractParamDataReaderWriter<ParamDataItem> {

    public ParamDataReaderWriter(Project project) {
        this(project, new DefaultIdGenerator());
    }

    public ParamDataReaderWriter(Project project, IdGenerator idGenerator) {
        super(project, idGenerator);
        Objects.requireNonNull(project);
    }

    @Override
    protected Data<ParamDataItem> read(String qualifiedName, boolean shareable, boolean useCache) {
        Objects.requireNonNull(qualifiedName);

        String key = genKey(qualifiedName, shareable);
        String filePath = genFilePath(key);

        if (useCache) {
            return readAndCache(key, filePath).cloneData();
        }

        return doRead(key, filePath);
    }

    @Override
    protected Future<?> write(Data<ParamDataItem> data) {

        Objects.requireNonNull(data);
        Objects.requireNonNull(data.getIdentity());

        autoGenerateIdIfNecessary(data);
        autoUpdateLastModifiedTime(data);

        return doWrite(data.getIdentity(), genFilePath(data.getIdentity()), data);
    }

    private void autoUpdateLastModifiedTime(Data<ParamDataItem> data) {
        long millis = System.currentTimeMillis();
        data.setTimestamp(millis);
        data.getDataList()
                .forEach(item -> item.setUpdateTime(millis));
    }

    private void autoGenerateIdIfNecessary(Data<ParamDataItem> data) {
        data.getDataList()
                .forEach(item -> {
                    if (Objects.isNull(item.getId())) {
                        item.setId(idGenerator.nextId());
                    }
                });
    }

    @Override
    protected String genFilePath(String key) {
        Objects.requireNonNull(key);
        String shareable = "";
        if (key.endsWith(SHAREABLE_SUFFIX)) {
            key = key.substring(0, key.length() - SHAREABLE_SUFFIX.length());
            shareable = SHAREABLE_SUFFIX;
        }
        if (!isValidClassName(key)) {
            throw new IllegalArgumentException("invalid class name");
        }

        String filepath = key.replace(".", FILE_SEPARATOR) + shareable + ".json";

        return project.getBasePath() + DATA_BASE_DIR + FILE_SEPARATOR + filepath;
    }

    @Override
    protected String genKey(String qualifiedName, boolean shareable) {
        return shareable ? qualifiedName + SHAREABLE_SUFFIX : qualifiedName;
    }
}
