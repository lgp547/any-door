package io.github.lgp547.anydoorplugin.data;

import java.util.Objects;
import java.util.concurrent.Future;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.github.lgp547.anydoorplugin.data.domain.Data;
import io.github.lgp547.anydoorplugin.data.domain.DataItem;
import io.github.lgp547.anydoorplugin.data.impl.DefaultIdGenerator;
import io.github.lgp547.anydoorplugin.util.JsonUtil;

/**
 * @description:
 * @author: zhouh
 * @date: 2023-07-22 11:41
 **/
public abstract class AbstractParamDataReaderWriter<T extends DataItem> extends AbstractDataReaderWriter<T> implements DataPersistent<T> {

    private static final Logger log = Logger.getInstance(AbstractParamDataReaderWriter.class);

    protected static final String SHAREABLE_SUFFIX = "&shareable";
    protected static final String FILE_SEPARATOR = System.getProperty("file.separator");
    protected final Project project;
    protected final IdGenerator idGenerator;

    public AbstractParamDataReaderWriter(Project project) {
        this(project, new DefaultIdGenerator());
    }

    public AbstractParamDataReaderWriter(Project project, IdGenerator idGenerator) {
        Objects.requireNonNull(project);
        this.project = project;
        this.idGenerator = idGenerator;
    }

    @Override
    public Future<?> saveAsync(Data<T> data) {
        log.info("saveOrUpdate " + data.getIdentity());
        log.debug("saveOrUpdate " + JsonUtil.toStrNotExc(data));
        return write(data);
    }

    @Override
    public Data<T> load(String identity) {
        return load(identity, false);
    }

    @Override
    public Data<T> load(String identity, boolean shareable) {
        return load(identity, shareable, true);
    }

    @Override
    public Data<T> load(String identity, boolean shareable, boolean useCache) {
        LOG.info(String.format("load %s, shareable: %s, useCache: %s", identity, shareable, useCache));
        return read(identity, shareable, useCache);
    }

    protected abstract Data<T> read(String qualifiedName, boolean shareable, boolean useCache);

    protected abstract Future<?> write(Data<T> data);

    protected abstract String genFilePath(String key);

    protected abstract String genKey(String identity, boolean shareable);

    protected boolean isValidClassName(String className) {
        return className.matches("([a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*");
    }
}
