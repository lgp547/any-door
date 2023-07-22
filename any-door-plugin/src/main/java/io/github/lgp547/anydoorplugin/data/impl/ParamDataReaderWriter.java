package io.github.lgp547.anydoorplugin.data.impl;

import java.util.Objects;
import java.util.concurrent.Future;

import com.esotericsoftware.minlog.Log;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.AbstractDataReaderWriter;
import io.github.lgp547.anydoorplugin.data.DataPersistent;
import io.github.lgp547.anydoorplugin.data.IdGenerator;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.ParamDataItem;
import io.github.lgp547.anydoorplugin.util.JsonUtil;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-06-29 11:34
 **/
public class ParamDataReaderWriter extends AbstractDataReaderWriter<ParamDataItem> implements DataPersistent<ParamDataItem> {

//    private static final Logger LOG = Logger.getInstance(ParamDataReaderWriter.class);
    private static final String SHAREABLE_SUFFIX = "&shareable";
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");
    private final Project project;
    private final IdGenerator idGenerator;

    public ParamDataReaderWriter(Project project) {
        this(project, new DefaultIdGenerator());
    }

    public ParamDataReaderWriter(Project project, IdGenerator idGenerator) {
        Objects.requireNonNull(project);
        this.project = project;
        this.idGenerator = idGenerator;
    }

    @Override
    public Future<?> saveAsync(Data<ParamDataItem> data) {
        Log.info("saveOrUpdate " + data.getIdentity());
        Log.debug("saveOrUpdate " + JsonUtil.toStrNotExc(data));
        return write(data);
    }

    @Override
    public Data<ParamDataItem> load(String identity) {
        return load(identity, false);
    }

    @Override
    public Data<ParamDataItem> load(String identity, boolean shareable) {
        return load(identity, shareable, true);
    }

    @Override
    public Data<ParamDataItem> load(String identity, boolean shareable, boolean useCache) {
        LOG.info(String.format("load %s, shareable: %s, useCache: %s", identity, shareable, useCache));
        return read(identity, shareable, useCache);
    }

    protected Data<ParamDataItem> read(String qualifiedName, boolean shareable, boolean useCache) {
        Objects.requireNonNull(qualifiedName);

        String key = genKey(qualifiedName, shareable);
        String filePath = genFilePath(key);

        if (useCache) {
            return readAndCache(key, filePath).cloneData();
        }

        return doRead(key, filePath);
    }

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

    private String genFilePath(String key) {
        Objects.requireNonNull(key);
        String shareable =  "";
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

    private boolean isValidClassName(String className) {
        return className.matches("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*");
    }

    private String genKey(String qualifiedName, boolean shareable) {
        return shareable ? qualifiedName + SHAREABLE_SUFFIX : qualifiedName;
    }
}
