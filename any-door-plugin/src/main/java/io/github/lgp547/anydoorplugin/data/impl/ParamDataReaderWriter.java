package io.github.lgp547.anydoorplugin.data.impl;

import java.util.Objects;
import java.util.concurrent.Future;

import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.AbstractParamDataReaderWriter;
import io.github.lgp547.anydoorplugin.data.IdGenerator;
import io.github.lgp547.anydoorplugin.data.domain.CacheData;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.settings.AnyDoorSettingsState;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-29 11:34
 **/
public class ParamDataReaderWriter extends AbstractParamDataReaderWriter<ParamDataItem> {

    private final AnyDoorSettingsState settingsState;

    public ParamDataReaderWriter(Project project) {
        this(project, new DefaultIdGenerator());
    }

    public ParamDataReaderWriter(Project project, IdGenerator idGenerator) {
        super(project, idGenerator);
        Objects.requireNonNull(project);
        settingsState = project.getService(AnyDoorSettingsState.class);
    }

    @Override
    protected Data<ParamDataItem> read(String qualifiedName, boolean shareable, boolean useCache) {
        Objects.requireNonNull(qualifiedName);

        String key = genKey(qualifiedName, shareable);
        String filePath = genFilePath(key);

        if (useCache) {
            return readAndCache(key, filePath).cloneData();
        }

        Data<ParamDataItem> data = doRead(key, filePath);
        dataCache.put(key, new CacheData<>(key, data));
        return data;
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


        return project.getBasePath() + settingsState.dataFileDir + FILE_SEPARATOR + filepath;
    }

    @Override
    protected String genKey(String qualifiedName, boolean shareable) {
        return shareable ? qualifiedName + SHAREABLE_SUFFIX : qualifiedName;
    }
}
